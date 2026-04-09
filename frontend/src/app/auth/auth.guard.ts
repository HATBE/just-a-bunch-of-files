import { CanActivateFn } from '@angular/router';
import { inject } from '@angular/core';
import { AuthService } from './auth.service';

export const authGuard: CanActivateFn = async () => {
  const authService = inject(AuthService);

  const token = await authService.getValidAccessToken();
  if (token) {
    return true;
  }

  await authService.startLoginRedirect();
  return false;
};
