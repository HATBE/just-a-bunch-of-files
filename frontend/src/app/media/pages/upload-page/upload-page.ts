import { Component, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { ApiError } from '../../../core/http.service';
import { MediaService } from '../../media.service';

@Component({
  selector: 'app-upload-page',
  imports: [FormsModule],
  templateUrl: './upload-page.html',
  styleUrl: './upload-page.css',
})
export class UploadPage {
  private static readonly MAX_FILES_PER_BATCH = 20;
  private static readonly MAX_BATCH_SIZE_BYTES = 512 * 1024 * 1024;

  private readonly mediaService = inject(MediaService);
  private readonly router = inject(Router);

  protected readonly userId = signal('');
  protected readonly albumIds = signal('');
  protected readonly isUploading = signal(false);
  protected readonly successMessage = signal('');
  protected readonly errorMessage = signal('');
  protected selectedFiles: File[] = [];

  protected onFileSelected(event: Event): void {
    const input = event.target as HTMLInputElement;
    this.selectedFiles = Array.from(input.files ?? []);
    this.successMessage.set('');
    this.errorMessage.set('');
  }

  protected async upload(): Promise<void> {
    if (!this.userId().trim()) {
      this.errorMessage.set('User ID is required.');
      return;
    }

    if (this.selectedFiles.length === 0) {
      this.errorMessage.set('Select at least one file first.');
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

      const batches = this.createBatches(this.selectedFiles);
      let uploadedCount = 0;

      for (let index = 0; index < batches.length; index++) {
        this.successMessage.set(`Uploading batch ${index + 1} of ${batches.length}...`);

        const uploaded = await this.mediaService.upload({
          userId: this.userId().trim(),
          albumIds,
          files: batches[index],
        });

        uploadedCount += uploaded.length;
      }

      this.successMessage.set(`Uploaded ${uploadedCount} file(s) in ${batches.length} batch(es).`);
      this.selectedFiles = [];
      this.albumIds.set('');
      await this.router.navigateByUrl('/images');
    } catch (error) {
      const message = this.readErrorMessage(error);
      this.errorMessage.set(message);
    } finally {
      this.isUploading.set(false);
    }
  }

  protected getBatchCount(): number {
    return this.createBatches(this.selectedFiles).length;
  }

  private createBatches(files: File[]): File[][] {
    const batches: File[][] = [];
    let currentBatch: File[] = [];
    let currentBatchSize = 0;

    for (const file of files) {
      const exceedsFileCountLimit = currentBatch.length >= UploadPage.MAX_FILES_PER_BATCH;
      const exceedsSizeLimit =
        currentBatch.length > 0 &&
        currentBatchSize + file.size > UploadPage.MAX_BATCH_SIZE_BYTES;

      if (exceedsFileCountLimit || exceedsSizeLimit) {
        batches.push(currentBatch);
        currentBatch = [];
        currentBatchSize = 0;
      }

      currentBatch.push(file);
      currentBatchSize += file.size;
    }

    if (currentBatch.length > 0) {
      batches.push(currentBatch);
    }

    return batches;
  }

  private readErrorMessage(error: unknown): string {
    if (this.isApiError(error)) {
      return error.message || `Upload failed (${error.status})`;
    }

    if (error instanceof Error) {
      return error.message;
    }

    return 'Upload failed';
  }

  private isApiError(error: unknown): error is ApiError {
    return typeof error === 'object' && error !== null && 'status' in error && 'message' in error;
  }
}
