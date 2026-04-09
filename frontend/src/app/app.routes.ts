import { Routes } from '@angular/router';
import { GalleryPage } from './media/pages/gallery-page/gallery-page';
import { UploadPage } from './media/pages/upload-page/upload-page';
import { LoginPage } from './auth/pages/login-page/login-page';
import { authGuard } from './auth/auth.guard';
import { AuthCallbackPage } from './auth/pages/auth-callback-page/auth-callback-page';
import { permissionGuard } from './auth/permission.guard';

export const routes: Routes = [
  { path: '', pathMatch: 'full', redirectTo: 'images' },
  { path: 'login', component: LoginPage },
  { path: 'auth/callback', component: AuthCallbackPage },
  { path: 'images', component: GalleryPage, canActivate: [authGuard] },
  { path: 'upload', component: UploadPage, canActivate: [permissionGuard('create_mediafile')] }
];
