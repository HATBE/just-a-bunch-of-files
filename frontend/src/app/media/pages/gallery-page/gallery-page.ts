import { Component, ElementRef, HostListener, OnDestroy, ViewChild, inject, signal } from '@angular/core';
import { ApiError } from '../../../core/http.service';
import { MediaService } from '../../media.service';
import { MediaListResponseDto } from '../../media.dtos';

@Component({
  selector: 'app-gallery-page',
  templateUrl: './gallery-page.html',
  styleUrl: './gallery-page.css',
})
export class GalleryPage implements OnDestroy {
  private static readonly PAGE_SIZE = 24;
  private static readonly MIN_ROW_HEIGHT = 110;
  private static readonly ROW_GAP = 12;
  private static readonly SECTION_GAP = 20;
  private static readonly SECTION_MIN_WIDTH = 260;
  private static readonly SECTION_MAX_WIDTH_RATIO = 0.78;
  private static readonly MIN_ASPECT_RATIO = 0.65;
  private static readonly MAX_ASPECT_RATIO = 1.8;
  private static readonly LOAD_MORE_THRESHOLD_PX = 1000;
  private static readonly MIN_PAGE_OVERFLOW_PX = 280;
  private static readonly SECTION_DATE_FORMAT = new Intl.DateTimeFormat(undefined, {
    day: '2-digit',
    month: '2-digit',
    year: 'numeric',
  });

  private readonly mediaService = inject(MediaService);
  private resizeObserver?: ResizeObserver;
  private readonly aspectRatios = new Map<string, number>();
  private autofillScheduled = false;
  private autofillInFlight = false;

  @ViewChild('gallerySurface')
  private set gallerySurfaceRef(value: ElementRef<HTMLElement> | undefined) {
    this.gallerySurface = value;
    this.resizeObserver?.disconnect();

    const surface = value?.nativeElement;
    if (!surface) {
      return;
    }

    this.resizeObserver = new ResizeObserver(() => {
      this.rebuildSections();
    });
    this.resizeObserver.observe(surface);
    this.rebuildSections();
    this.scheduleAutofillCheck();
  }

  private gallerySurface?: ElementRef<HTMLElement>;

  protected readonly items = signal<MediaListResponseDto[]>([]);
  protected readonly sections = signal<GallerySectionSlice[]>([]);
  protected readonly isLoading = signal(true);
  protected readonly isLoadingMore = signal(false);
  protected readonly hasMore = signal(false);
  protected readonly errorMessage = signal('');

  constructor() {
    void this.load();
  }

  public ngOnDestroy(): void {
    this.resizeObserver?.disconnect();
  }

  @HostListener('window:scroll')
  protected onWindowScroll(): void {
    this.scheduleAutofillCheck();
  }

  protected getPreviewUrl(fileId: string): string {
    return this.mediaService.getPreviewUrl(fileId);
  }

  protected onPreviewLoad(fileId: string, event: Event): void {
    const image = event.target as HTMLImageElement | null;
    if (!image || image.naturalWidth <= 0 || image.naturalHeight <= 0) {
      return;
    }

    const aspectRatio = image.naturalWidth / image.naturalHeight;
    this.aspectRatios.set(fileId, this.normalizeAspectRatio(aspectRatio));
    this.rebuildSections();
    this.scheduleAutofillCheck();
  }

  protected async refresh(): Promise<void> {
    await this.load(true);
  }

  protected async loadMore(): Promise<void> {
    if (this.isLoading() || this.isLoadingMore() || !this.hasMore()) {
      return;
    }

    await this.load(false);
  }

  private async load(reset = true): Promise<void> {
    if (reset) {
      this.isLoading.set(true);
      this.errorMessage.set('');
    } else {
      this.isLoadingMore.set(true);
    }

    try {
      const offset = reset ? 0 : this.items().length;
      const response = await this.mediaService.getAll(undefined, GalleryPage.PAGE_SIZE, offset);
      const nextItems = reset
        ? response.items
        : [...this.items(), ...response.items];

      this.items.set(nextItems);
      this.hasMore.set(response.hasMore);
      this.rebuildSections();
      this.scheduleAutofillCheck();
    } catch (error) {
      const message = this.readErrorMessage(error);
      this.errorMessage.set(message);
    } finally {
      if (reset) {
        this.isLoading.set(false);
      } else {
        this.isLoadingMore.set(false);
      }
    }
  }

  private readErrorMessage(error: unknown): string {
    if (this.isApiError(error)) {
      return error.message || `Failed to load media (${error.status})`;
    }

    if (error instanceof Error) {
      return error.message;
    }

    return 'Failed to load media';
  }

  private isApiError(error: unknown): error is ApiError {
    return typeof error === 'object' && error !== null && 'status' in error && 'message' in error;
  }

  private rebuildSections(): void {
    const surfaceWidth = this.gallerySurface?.nativeElement.clientWidth ?? 0;
    const items = this.items();

    if (surfaceWidth <= 0 || items.length === 0) {
      this.sections.set([]);
      return;
    }

    const groupedItems = new Map<string, MediaListResponseDto[]>();
    for (const item of items) {
      const sectionKey = this.sectionKeyFor(item);
      const sectionItems = groupedItems.get(sectionKey) ?? [];
      sectionItems.push(item);
      groupedItems.set(sectionKey, sectionItems);
    }

    this.sections.set(this.buildPackedSlices(groupedItems, surfaceWidth));
  }

  private buildPackedSlices(
    groupedItems: Map<string, MediaListResponseDto[]>,
    surfaceWidth: number,
  ): GallerySectionSlice[] {
    const slices: GallerySectionSlice[] = [];
    let remainingShelfWidth = surfaceWidth;
    const shelfHeight = this.targetRowHeight(surfaceWidth);

    for (const [sectionKey, sectionItems] of groupedItems.entries()) {
      const queue = [...sectionItems];
      const label = this.sectionLabelFor(sectionItems[0]);
      let sliceIndex = 0;

      while (queue.length > 0) {
        if (remainingShelfWidth < GalleryPage.SECTION_MIN_WIDTH) {
          remainingShelfWidth = surfaceWidth;
        }

        const desiredWidth = Math.min(
          remainingShelfWidth,
          this.estimateSectionWidth(queue, surfaceWidth, shelfHeight),
        );
        const slice = this.buildSectionSlice(
          `${sectionKey}-${sliceIndex}`,
          label,
          queue,
          desiredWidth,
          shelfHeight,
          sliceIndex === 0,
        );

        if (slice.width > remainingShelfWidth && remainingShelfWidth < surfaceWidth) {
          remainingShelfWidth = surfaceWidth;
          continue;
        }

        slices.push(slice);
        queue.splice(0, slice.row.items.length);
        sliceIndex += 1;
        remainingShelfWidth -= slice.width + GalleryPage.SECTION_GAP;
      }
    }

    return slices;
  }

  private buildSectionSlice(
    key: string,
    label: string,
    items: MediaListResponseDto[],
    desiredWidth: number,
    rowHeight: number,
    showLabel: boolean,
  ): GallerySectionSlice {
    const rowItems: MediaListResponseDto[] = [];
    let projectedWidth = 0;

    for (const item of items) {
      const itemWidth = this.itemWidth(item, rowHeight);
      const nextWidth = rowItems.length === 0
        ? itemWidth
        : projectedWidth + GalleryPage.ROW_GAP + itemWidth;

      if (rowItems.length > 0 && nextWidth > desiredWidth) {
        break;
      }

      rowItems.push(item);
      projectedWidth = nextWidth;
    }

    if (rowItems.length === 0 && items.length > 0) {
      rowItems.push(items[0]);
    }

    const row = this.buildRow(rowItems, rowHeight);
    return {
      key,
      label,
      showLabel,
      width: this.rowWidth(row),
      row,
    };
  }

  private estimateSectionWidth(items: MediaListResponseDto[], surfaceWidth: number, rowHeight: number): number {
    const maxWidth = Math.floor(surfaceWidth * GalleryPage.SECTION_MAX_WIDTH_RATIO);
    const visibleItems = items.slice(0, Math.min(items.length, 6));
    const preferredWidth = Math.round(
      visibleItems.reduce((sum, item) => sum + this.itemWidth(item, rowHeight), 0)
      + Math.max(0, visibleItems.length - 1) * GalleryPage.ROW_GAP,
    );

    return Math.min(maxWidth, Math.max(GalleryPage.SECTION_MIN_WIDTH, preferredWidth));
  }

  private buildRow(items: MediaListResponseDto[], rowHeight: number): GalleryRow {
    const height = Math.max(GalleryPage.MIN_ROW_HEIGHT, Math.round(rowHeight));
    return {
      items: items.map((item) => ({
        item,
        width: this.itemWidth(item, height),
      })),
      height,
    };
  }

  private itemWidth(item: MediaListResponseDto, rowHeight: number): number {
    return Math.max(90, Math.round(rowHeight * this.resolveAspectRatio(item)));
  }

  private rowWidth(row: GalleryRow): number {
    return row.items.reduce((sum, entry) => sum + entry.width, 0)
      + GalleryPage.ROW_GAP * Math.max(0, row.items.length - 1);
  }

  private scheduleAutofillCheck(): void {
    if (this.autofillScheduled) {
      return;
    }

    this.autofillScheduled = true;
    queueMicrotask(() => {
      this.autofillScheduled = false;
      void this.ensureViewportFilled();
    });
  }

  private async ensureViewportFilled(): Promise<void> {
    if (
      this.autofillInFlight ||
      this.isLoading() ||
      this.isLoadingMore() ||
      !this.hasMore()
    ) {
      return;
    }

    const pageHeight = document.documentElement.scrollHeight;
    const viewportBottom = window.scrollY + window.innerHeight;
    const remainingScroll = pageHeight - viewportBottom;
    const needsMore =
      pageHeight <= window.innerHeight + GalleryPage.MIN_PAGE_OVERFLOW_PX ||
      remainingScroll <= GalleryPage.LOAD_MORE_THRESHOLD_PX;

    if (!needsMore) {
      return;
    }

    this.autofillInFlight = true;
    try {
      await this.load(false);
    } finally {
      this.autofillInFlight = false;
      if (this.hasMore()) {
        this.scheduleAutofillCheck();
      }
    }
  }

  private resolveAspectRatio(item: MediaListResponseDto): number {
    const known = this.aspectRatios.get(item.fileId);
    if (known != null) {
      return known;
    }

    if (item.kind === 'VIDEO') {
      return 16 / 9;
    }

    return 4 / 3;
  }

  private normalizeAspectRatio(aspectRatio: number): number {
    return Math.min(
      GalleryPage.MAX_ASPECT_RATIO,
      Math.max(GalleryPage.MIN_ASPECT_RATIO, aspectRatio),
    );
  }

  private targetRowHeight(containerWidth: number): number {
    if (containerWidth >= 520) {
      return 264;
    }

    if (containerWidth >= 420) {
      return 276;
    }

    if (containerWidth >= 340) {
      return 250;
    }

    return 166;
  }

  private sectionKeyFor(item: MediaListResponseDto): string {
    const date = this.displayDateFor(item);
    return [
      date.getFullYear(),
      date.getMonth(),
      date.getDate(),
    ].join('-');
  }

  private sectionLabelFor(item: MediaListResponseDto): string {
    const date = this.displayDateFor(item);
    const today = new Date();
    const yesterday = new Date();
    yesterday.setDate(today.getDate() - 1);

    if (this.isSameDay(date, today)) {
      return 'Today';
    }

    if (this.isSameDay(date, yesterday)) {
      return 'Yesterday';
    }

    return GalleryPage.SECTION_DATE_FORMAT.format(date);
  }

  private displayDateFor(item: MediaListResponseDto): Date {
    return new Date(item.capturedAt ?? item.uploadedAt);
  }

  private isSameDay(left: Date, right: Date): boolean {
    return left.getFullYear() === right.getFullYear()
      && left.getMonth() === right.getMonth()
      && left.getDate() === right.getDate();
  }
}

type GallerySectionSlice = {
  key: string;
  label: string;
  showLabel: boolean;
  width: number;
  row: GalleryRow;
};

type GalleryRow = {
  items: GalleryRowItem[];
  height: number;
};

type GalleryRowItem = {
  item: MediaListResponseDto;
  width: number;
};
