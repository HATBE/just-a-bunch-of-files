import { Component, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { MediaService } from '../../media.service';

@Component({
  selector: 'app-upload-page',
  imports: [FormsModule],
  templateUrl: './upload-page.html',
  styleUrl: './upload-page.css',
})
export class UploadPage {
  private readonly mediaService = inject(MediaService);
  private readonly router = inject(Router);

  protected readonly userId = signal('');
  protected readonly albumIds = signal('');
  protected readonly isUploading = signal(false);
  protected readonly successMessage = signal('');
  protected readonly errorMessage = signal('');
  protected selectedFile: File | null = null;

  protected onFileSelected(event: Event): void {
    const input = event.target as HTMLInputElement;
    this.selectedFile = input.files?.[0] ?? null;
    this.successMessage.set('');
    this.errorMessage.set('');
  }

  protected async upload(): Promise<void> {
    if (!this.userId().trim()) {
      this.errorMessage.set('User ID is required.');
      return;
    }

    if (!this.selectedFile) {
      this.errorMessage.set('Select a file first.');
      return;
    }

    this.isUploading.set(true);
    this.successMessage.set('');
    this.errorMessage.set('');

    try {
      const albumIds = this.albumIds()
        .split(',')
        .map((value) => value.trim())
        .filter(Boolean);

      await this.mediaService.upload({
        userId: this.userId().trim(),
        albumIds,
        file: this.selectedFile,
      });

      this.successMessage.set('Upload completed.');
      this.selectedFile = null;
      this.albumIds.set('');
      await this.router.navigateByUrl('/images');
    } catch (error) {
      const message = error instanceof Error ? error.message : 'Upload failed';
      this.errorMessage.set(message);
    } finally {
      this.isUploading.set(false);
    }
  }
}
