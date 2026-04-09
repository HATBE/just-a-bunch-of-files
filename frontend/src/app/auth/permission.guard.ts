import { CanActivateFn, Router } from '@angular/router';
import { inject } from '@angular/core';
import { AuthService } from './auth.service';

export function permissionGuard(permission: string): CanActivateFn {
  return async () => {
    const authService = inject(AuthService);
    const router = inject(Router);

    const token = await authService.getValidAccessToken();
    if (!token) {
      await authService.startLoginRedirect();
      return false;
    }

    if (authService.hasPermission(permission)) {
      return true;
    }

    return router.createUrlTree(['/images']);
  };
}
