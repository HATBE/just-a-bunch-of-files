import { Component, inject } from '@angular/core';
import { Router } from '@angular/router';
import { AuthService } from '../../auth.service';

@Component({
  selector: 'app-login-page',
  template: ''
})
export class LoginPage {
  private readonly authService = inject(AuthService);
  private readonly router = inject(Router);

  constructor() {
    void this.redirect();
  }

  private async redirect(): Promise<void> {
    const token = await this.authService.getValidAccessToken();
    if (token) {
      await this.router.navigateByUrl('/images');
      return;
    }

    await this.authService.startLoginRedirect();
  }
}
