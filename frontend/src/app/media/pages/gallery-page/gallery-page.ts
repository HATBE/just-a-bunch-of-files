import { DatePipe } from '@angular/common';
import { Component, inject, signal } from '@angular/core';
import { MediaService } from '../../media.service';
import { MediaListResponseDto } from '../../media.dtos';

@Component({
  selector: 'app-gallery-page',
  imports: [DatePipe],
  templateUrl: './gallery-page.html',
  styleUrl: './gallery-page.css',
})
export class GalleryPage {
  private readonly mediaService = inject(MediaService);

  protected readonly items = signal<MediaListResponseDto[]>([]);
  protected readonly isLoading = signal(true);
  protected readonly errorMessage = signal('');

  constructor() {
    void this.load();
  }

  protected getMediaUrl(fileId: string): string {
    return this.mediaService.getContentUrl(fileId);
  }

  protected async refresh(): Promise<void> {
    await this.load();
  }

  private async load(): Promise<void> {
    this.isLoading.set(true);
    this.errorMessage.set('');

    try {
      const items = await this.mediaService.getAll();
      this.items.set(items);
    } catch (error) {
      const message = error instanceof Error ? error.message : 'Failed to load images';
      this.errorMessage.set(message);
    } finally {
      this.isLoading.set(false);
    }
  }
}
