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
  private static readonly MAX_ITEMS_PER_ROW = 10;
  private static readonly MAX_ITEMS_PER_SMALL_ROW = 4;
  private static readonly ROW_GAP = 12;
  private static readonly MIN_SPARSE_ROW_HEIGHT = 72;
  private static readonly MAX_SPARSE_ROW_HEIGHT = 180;
  private static readonly SMALL_SECTION_ITEM_COUNT = 6;
  private static readonly MIN_ASPECT_RATIO = 0.45;
  private static readonly MAX_ASPECT_RATIO = 2.4;
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
  private readonly thumbnailUrls = new Map<string, string>();
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
  protected readonly sections = signal<GallerySection[]>([]);
  protected readonly isLoading = signal(true);
  protected readonly isLoadingMore = signal(false);
  protected readonly hasMore = signal(false);
  protected readonly errorMessage = signal('');

  constructor() {
    void this.load();
  }

  public ngOnDestroy(): void {
    this.resizeObserver?.disconnect();
    for (const url of this.thumbnailUrls.values()) {
      URL.revokeObjectURL(url);
    }
  }

  @HostListener('window:scroll')
  protected onWindowScroll(): void {
    this.scheduleAutofillCheck();
  }

  protected thumbnailUrl(fileId: string): string | null {
    return this.thumbnailUrls.get(fileId) ?? null;
  }

  protected onThumbnailLoad(fileId: string, event: Event): void {
    const image = event.target as HTMLImageElement | null;
    if (!image || image.naturalWidth <= 0 || image.naturalHeight <= 0) {
      return;
    }

    const aspectRatio = image.naturalWidth / image.naturalHeight;
    this.aspectRatios.set(fileId, this.normalizeAspectRatio(aspectRatio));
    this.rebuildSections();
    this.scheduleAutofillCheck();
  }

  protected onThumbnailError(fileId: string): void {
    this.thumbnailUrls.delete(fileId);
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
      const page = reset ? 0 : Math.floor(this.items().length / GalleryPage.PAGE_SIZE);
      const response = await this.mediaService.getAll(GalleryPage.PAGE_SIZE, page);
      const nextItems = reset
        ? response.content
        : [...this.items(), ...response.content];

      this.items.set(nextItems);
      this.hasMore.set(!response.last);
      this.primeThumbnailUrls(nextItems);
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

    this.sections.set(this.buildSections(groupedItems, surfaceWidth));
  }

  private buildSections(
    groupedItems: Map<string, MediaListResponseDto[]>,
    surfaceWidth: number,
  ): GallerySection[] {
    const sections: GallerySection[] = [];

    for (const [sectionKey, sectionItems] of groupedItems.entries()) {
      sections.push({
        key: sectionKey,
        label: this.sectionLabelFor(sectionItems[0]),
        rows: this.buildJustifiedRows(sectionItems, surfaceWidth, sectionKey),
      });
    }

    return sections;
  }

  private buildJustifiedRows(
    items: MediaListResponseDto[],
    containerWidth: number,
    sectionKey: string,
  ): GalleryRow[] {
    if (items.length <= GalleryPage.SMALL_SECTION_ITEM_COUNT) {
      return this.buildSmallSectionRows(items, containerWidth, sectionKey);
    }

    const rows: GalleryRow[] = [];
    const targetRowHeight = this.targetRowHeight(containerWidth);
    let rowItems: MediaListResponseDto[] = [];
    let aspectSum = 0;
    let rowIndex = 0;

    for (const item of items) {
      rowItems.push(item);
      aspectSum += this.resolveAspectRatio(item);

      const projectedWidth = this.projectedRowWidth(rowItems.length, aspectSum, targetRowHeight);
      const reachedTarget = projectedWidth >= containerWidth;
      const reachedItemLimit = rowItems.length >= GalleryPage.MAX_ITEMS_PER_ROW;

      if (!reachedTarget && !reachedItemLimit) {
        continue;
      }

      rows.push(this.buildRow(
        rowItems,
        this.computeExactRowHeight(containerWidth, rowItems),
        containerWidth,
        `${sectionKey}-${rowIndex}`,
        true,
      ));
      rowItems = [];
      aspectSum = 0;
      rowIndex += 1;
    }

    if (rowItems.length > 0) {
      const sparseRowHeight = Math.min(this.computeExactRowHeight(containerWidth, rowItems), targetRowHeight);
      rows.push(this.buildRow(
        rowItems,
        rowItems.length <= 3
          ? Math.max(GalleryPage.MIN_SPARSE_ROW_HEIGHT, sparseRowHeight)
          : sparseRowHeight,
        containerWidth,
        `${sectionKey}-${rowIndex}`,
        false,
      ));
    }

    return rows;
  }

  private buildSmallSectionRows(
    items: MediaListResponseDto[],
    containerWidth: number,
    sectionKey: string,
  ): GalleryRow[] {
    const rows: GalleryRow[] = [];

    for (let index = 0; index < items.length; index += GalleryPage.MAX_ITEMS_PER_SMALL_ROW) {
      const rowItems = items.slice(index, index + GalleryPage.MAX_ITEMS_PER_SMALL_ROW);
      const rowHeight = Math.min(
        GalleryPage.MAX_SPARSE_ROW_HEIGHT,
        Math.max(
          GalleryPage.MIN_SPARSE_ROW_HEIGHT,
          this.computeExactRowHeight(containerWidth, rowItems),
        ),
      );
      rows.push(this.buildRow(
        rowItems,
        rowHeight,
        containerWidth,
        `${sectionKey}-small-${index}`,
        rowItems.length > 1,
      ));
    }

    return rows;
  }

  private buildRow(
    items: MediaListResponseDto[],
    rowHeight: number,
    containerWidth: number,
    key: string,
    justify: boolean,
  ): GalleryRow {
    const availableWidth = containerWidth - GalleryPage.ROW_GAP * Math.max(0, items.length - 1);
    const widths = items.map((item) => this.resolveAspectRatio(item) * rowHeight);
    const finalWidths = justify
      ? this.justifyWidths(widths, availableWidth)
      : widths;

    return {
      key,
      items: items.map((item, index) => ({
        item,
        width: finalWidths[index],
      })),
      height: rowHeight,
    };
  }

  private justifyWidths(widths: number[], availableWidth: number): number[] {
    const totalWidth = widths.reduce((sum, width) => sum + width, 0);
    if (totalWidth <= 0) {
      return widths;
    }

    const scale = availableWidth / totalWidth;
    return widths.map((width) => width * scale);
  }

  private computeExactRowHeight(containerWidth: number, items: MediaListResponseDto[]): number {
    const aspectSum = items.reduce((sum, item) => sum + this.resolveAspectRatio(item), 0);
    const availableWidth = containerWidth - GalleryPage.ROW_GAP * Math.max(0, items.length - 1);
    return availableWidth / Math.max(0.1, aspectSum);
  }

  private projectedRowWidth(itemCount: number, aspectSum: number, rowHeight: number): number {
    return aspectSum * rowHeight + GalleryPage.ROW_GAP * Math.max(0, itemCount - 1);
  }

  private targetRowHeight(containerWidth: number): number {
    if (containerWidth >= 1500) {
      return 50;
    }

    if (containerWidth >= 1100) {
      return 45;
    }

    if (containerWidth >= 800) {
      return 40;
    }

    return 34;
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
    const known = this.aspectRatios.get(item.mediaFileId);
    if (known != null) {
      return known;
    }

    return item.kind === 'VIDEO' ? 16 / 9 : 4 / 3;
  }

  private normalizeAspectRatio(aspectRatio: number): number {
    return Math.min(
      GalleryPage.MAX_ASPECT_RATIO,
      Math.max(GalleryPage.MIN_ASPECT_RATIO, aspectRatio),
    );
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
    const capturedDate = item.capturedAt ? new Date(item.capturedAt) : null;
    if (capturedDate && Number.isFinite(capturedDate.getTime())) {
      return capturedDate;
    }

    const uploadedDate = item.uploadedAt ? new Date(item.uploadedAt) : null;
    const parsedDate = uploadedDate;

    if (parsedDate && Number.isFinite(parsedDate.getTime())) {
      return parsedDate;
    }

    return new Date(0);
  }

  private isSameDay(left: Date, right: Date): boolean {
    return left.getFullYear() === right.getFullYear()
      && left.getMonth() === right.getMonth()
      && left.getDate() === right.getDate();
  }

  private primeThumbnailUrls(items: MediaListResponseDto[]): void {
    for (const item of items) {
      if (item.kind !== 'IMAGE' || this.thumbnailUrls.has(item.mediaFileId)) {
        continue;
      }

      void this.mediaService.getThumbnailBlobUrl(item.mediaFileId)
        .then((url) => {
          const previous = this.thumbnailUrls.get(item.mediaFileId);
          if (previous) {
            URL.revokeObjectURL(previous);
          }
          this.thumbnailUrls.set(item.mediaFileId, url);
        })
        .catch(() => {
          this.thumbnailUrls.delete(item.mediaFileId);
        });
    }
  }
}

type GallerySection = {
  key: string;
  label: string;
  rows: GalleryRow[];
};

type GalleryRow = {
  key: string;
  items: GalleryRowItem[];
  height: number;
};

type GalleryRowItem = {
  item: MediaListResponseDto;
  width: number;
};
