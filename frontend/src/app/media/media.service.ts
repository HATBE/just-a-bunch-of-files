import { HttpParams } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { firstValueFrom } from 'rxjs';
import { HttpService } from '../core/http.service';
import {
  MediaDetailResponseDto,
  MediaPagedResponseDto,
  MediaListResponseDto,
  UploadMediaRequestDto
} from './media.dtos';

@Injectable({
  providedIn: 'root'
})
export class MediaService extends HttpService {
  constructor() {
    super();
    this.setEntity('media');
  }

  public getAll(userId?: string, limit = 24, offset = 0): Promise<MediaPagedResponseDto> {
    let params = new HttpParams()
      .set('limit', limit)
      .set('offset', offset);

    if (userId) {
      params = params.set('userId', userId);
    }

    return firstValueFrom(
      this.http.get<MediaPagedResponseDto>(this.createUrl(), { params })
    );
  }

  public getById(fileId: string): Promise<MediaDetailResponseDto> {
    return this.get<MediaDetailResponseDto>([fileId]);
  }

  public upload(request: UploadMediaRequestDto): Promise<MediaDetailResponseDto[]> {
    const payload = new FormData();
    payload.set('userId', request.userId);

    for (const file of request.files) {
      payload.append('files', file);
    }

    for (const albumId of request.albumIds ?? []) {
      payload.append('albumIds', albumId);
    }

    return firstValueFrom(
      this.http.post<MediaDetailResponseDto[]>(this.createUrl(), payload)
    );
  }

  public deleteById(fileId: string): Promise<void> {
    return this.delete<void>([fileId]);
  }

  public getContentUrl(fileId: string): string {
    return this.createUrl([fileId, 'content']);
  }

  public getPreviewUrl(fileId: string): string {
    return this.createUrl([fileId, 'preview']);
  }
}
