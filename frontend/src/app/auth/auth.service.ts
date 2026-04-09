import { Injectable, computed, signal } from '@angular/core';

type TokenResponse = {
  access_token: string;
  expires_in: number;
  refresh_expires_in: number;
  refresh_token: string;
  id_token?: string;
  token_type: string;
  scope: string;
};

type StoredSession = {
  accessToken: string;
  refreshToken: string;
  idToken?: string;
  accessTokenExpiresAt: number;
  refreshTokenExpiresAt: number;
};

type JwtClaims = {
  preferred_username?: string;
  email?: string;
  realm_access?: {
    roles?: string[];
  };
};

@Injectable({
  providedIn: 'root'
})
export class AuthService {
  private static readonly STORAGE_KEY = 'jbof.auth.session';
  private static readonly LOGIN_STATE_KEY = 'jbof.auth.login-state';
  private static readonly KEYCLOAK_BASE_URL = 'http://localhost:8081';
  private static readonly REALM = 'jbof';
  private static readonly CLIENT_ID = 'jbof-dev';
  private static readonly REDIRECT_URI = 'http://localhost:4200/auth/callback';

  private readonly session = signal<StoredSession | null>(this.readSession());
  private refreshPromise: Promise<string | null> | null = null;

  readonly isAuthenticated = computed(() => {
    const session = this.session();
    return session !== null && session.accessTokenExpiresAt > Date.now();
  });

  readonly username = computed(() => {
    const claims = this.readClaims();
    return claims?.preferred_username ?? claims?.email ?? '';
  });

  readonly permissions = computed(() => this.readClaims()?.realm_access?.roles ?? []);

  async startLoginRedirect(): Promise<never> {
    const verifier = this.randomString(96);
    const state = this.randomString(48);
    const challenge = await this.pkceChallenge(verifier);

    sessionStorage.setItem(AuthService.LOGIN_STATE_KEY, JSON.stringify({ verifier, state }));

    const params = new URLSearchParams({
      client_id: AuthService.CLIENT_ID,
      redirect_uri: AuthService.REDIRECT_URI,
      response_type: 'code',
      scope: 'openid profile email',
      code_challenge_method: 'S256',
      code_challenge: challenge,
      state
    });

    window.location.assign(this.authorizationUrl(params));
    throw new Error('Redirecting to login');
  }

  async completeLogin(callbackUrl: string): Promise<void> {
    const url = new URL(callbackUrl);
    const code = url.searchParams.get('code');
    const state = url.searchParams.get('state');
    const error = url.searchParams.get('error');

    if (error) {
      throw new Error(url.searchParams.get('error_description') ?? error);
    }

    if (!code || !state) {
      throw new Error('Missing login callback parameters');
    }

    const loginState = this.readLoginState();
    if (!loginState || loginState.state !== state) {
      throw new Error('Invalid login state');
    }

    const payload = new URLSearchParams({
      client_id: AuthService.CLIENT_ID,
      grant_type: 'authorization_code',
      code,
      redirect_uri: AuthService.REDIRECT_URI,
      code_verifier: loginState.verifier
    });

    const response = await fetch(this.tokenUrl(), {
      method: 'POST',
      headers: {
        'Content-Type': 'application/x-www-form-urlencoded'
      },
      body: payload.toString()
    });

    const body = await response.json();
    sessionStorage.removeItem(AuthService.LOGIN_STATE_KEY);

    if (!response.ok) {
      const message = body?.error_description ?? body?.error ?? 'Login failed';
      throw new Error(message);
    }

    this.storeSession(body as TokenResponse);
  }

  logout(): void {
    localStorage.removeItem(AuthService.STORAGE_KEY);
    sessionStorage.removeItem(AuthService.LOGIN_STATE_KEY);
    this.session.set(null);
  }

  logoutRedirect(): void {
    const idToken = this.session()?.idToken;
    this.logout();

    const params = new URLSearchParams({
      client_id: AuthService.CLIENT_ID,
      post_logout_redirect_uri: `${window.location.origin}/login`
    });

    if (idToken) {
      params.set('id_token_hint', idToken);
    }

    window.location.assign(`${this.logoutUrl()}?${params.toString()}`);
  }

  async getValidAccessToken(): Promise<string | null> {
    const currentSession = this.session();
    if (currentSession == null) {
      return null;
    }

    if (currentSession.accessTokenExpiresAt > Date.now() + 10_000) {
      return currentSession.accessToken;
    }

    if (currentSession.refreshTokenExpiresAt <= Date.now()) {
      this.logout();
      return null;
    }

    if (this.refreshPromise == null) {
      this.refreshPromise = this.refreshAccessToken();
    }

    try {
      return await this.refreshPromise;
    } finally {
      this.refreshPromise = null;
    }
  }

  registrationUrl(): string {
    return `${AuthService.KEYCLOAK_BASE_URL}/realms/${AuthService.REALM}/account/`;
  }

  hasPermission(permission: string): boolean {
    return this.permissions().includes(permission);
  }

  private async refreshAccessToken(): Promise<string | null> {
    const currentSession = this.session();
    if (currentSession == null) {
      return null;
    }

    const payload = new URLSearchParams({
      client_id: AuthService.CLIENT_ID,
      grant_type: 'refresh_token',
      refresh_token: currentSession.refreshToken
    });

    const response = await fetch(this.tokenUrl(), {
      method: 'POST',
      headers: {
        'Content-Type': 'application/x-www-form-urlencoded'
      },
      body: payload.toString()
    });

    const body = await response.json();
    if (!response.ok) {
      this.logout();
      return null;
    }

    this.storeSession(body as TokenResponse);
    return this.session()?.accessToken ?? null;
  }

  private tokenUrl(): string {
    return `${AuthService.KEYCLOAK_BASE_URL}/realms/${AuthService.REALM}/protocol/openid-connect/token`;
  }

  private logoutUrl(): string {
    return `${AuthService.KEYCLOAK_BASE_URL}/realms/${AuthService.REALM}/protocol/openid-connect/logout`;
  }

  private authorizationUrl(params: URLSearchParams): string {
    return `${AuthService.KEYCLOAK_BASE_URL}/realms/${AuthService.REALM}/protocol/openid-connect/auth?${params.toString()}`;
  }

  private storeSession(tokenResponse: TokenResponse): void {
    const now = Date.now();
    const session: StoredSession = {
      accessToken: tokenResponse.access_token,
      refreshToken: tokenResponse.refresh_token,
      idToken: tokenResponse.id_token,
      accessTokenExpiresAt: now + tokenResponse.expires_in * 1000,
      refreshTokenExpiresAt: now + tokenResponse.refresh_expires_in * 1000
    };

    localStorage.setItem(AuthService.STORAGE_KEY, JSON.stringify(session));
    this.session.set(session);
  }

  private readSession(): StoredSession | null {
    const rawSession = localStorage.getItem(AuthService.STORAGE_KEY);
    if (rawSession == null) {
      return null;
    }

    try {
      return JSON.parse(rawSession) as StoredSession;
    } catch {
      localStorage.removeItem(AuthService.STORAGE_KEY);
      return null;
    }
  }

  private readLoginState(): { verifier: string; state: string } | null {
    const rawState = sessionStorage.getItem(AuthService.LOGIN_STATE_KEY);
    if (!rawState) {
      return null;
    }

    try {
      return JSON.parse(rawState) as { verifier: string; state: string };
    } catch {
      sessionStorage.removeItem(AuthService.LOGIN_STATE_KEY);
      return null;
    }
  }

  private readClaims(): JwtClaims | null {
    const accessToken = this.session()?.accessToken;
    if (!accessToken) {
      return null;
    }

    const parts = accessToken.split('.');
    if (parts.length < 2) {
      return null;
    }

    try {
      const payload = parts[1]
        .replace(/-/g, '+')
        .replace(/_/g, '/')
        .padEnd(Math.ceil(parts[1].length / 4) * 4, '=');
      return JSON.parse(atob(payload)) as JwtClaims;
    } catch {
      return null;
    }
  }

  private randomString(length: number): string {
    const charset = 'ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789-._~';
    const bytes = new Uint8Array(length);
    crypto.getRandomValues(bytes);
    return Array.from(bytes, (value) => charset[value % charset.length]).join('');
  }

  private async pkceChallenge(verifier: string): Promise<string> {
    const buffer = await crypto.subtle.digest('SHA-256', new TextEncoder().encode(verifier));
    return btoa(String.fromCharCode(...new Uint8Array(buffer)))
      .replace(/\+/g, '-')
      .replace(/\//g, '_')
      .replace(/=+$/g, '');
  }
}
