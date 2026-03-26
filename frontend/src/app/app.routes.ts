import { Routes } from '@angular/router';
import { GalleryPage } from './media/pages/gallery-page/gallery-page';
import { UploadPage } from './media/pages/upload-page/upload-page';

export const routes: Routes = [
  { path: '', pathMatch: 'full', redirectTo: 'images' },
  { path: 'images', component: GalleryPage },
  { path: 'upload', component: UploadPage }
];
