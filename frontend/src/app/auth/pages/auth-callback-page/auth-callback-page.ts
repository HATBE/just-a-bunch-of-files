import { Component, inject, signal } from '@angular/core';
import { Router } from '@angular/router';
import { AuthService } from '../../auth.service';

@Component({
  selector: 'app-auth-callback-page',
  template: `
    <section class="page">
      @if (errorMessage()) {
        <p>{{ errorMessage() }}</p>
      } @else {
        <p>Signing you in...</p>
      }
    </section>
  `
})
export class AuthCallbackPage {
  private readonly authService = inject(AuthService);
  private readonly router = inject(Router);

  protected readonly errorMessage = signal('');

  constructor() {
    void this.finishLogin();
  }

  private async finishLogin(): Promise<void> {
    try {
      await this.authService.completeLogin(window.location.href);
      await this.router.navigateByUrl('/images');
    } catch (error) {
      this.errorMessage.set(error instanceof Error ? error.message : 'Login failed');
      await this.router.navigateByUrl('/login');
    }
  }
}
