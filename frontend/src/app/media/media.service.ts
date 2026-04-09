import { HttpParams } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { firstValueFrom } from 'rxjs';
import { HttpService } from '../core/http.service';
import {
  MediaDetailResponseDto,
  PageResponseDto,
  MediaListResponseDto,
  UploadMediaRequestDto
} from './media.dtos';

@Injectable({
  providedIn: 'root'
})
export class MediaService extends HttpService {
  constructor() {
    super();
    this.setEntity('media-files');
  }

  public getAll(size = 24, page = 0): Promise<PageResponseDto<MediaListResponseDto>> {
    const params = new HttpParams()
      .set('size', size)
      .set('page', page);

    return firstValueFrom(
      this.http.get<PageResponseDto<MediaListResponseDto>>(this.createUrl(), { params })
    );
  }

  public getById(fileId: string): Promise<MediaDetailResponseDto> {
    return this.get<MediaDetailResponseDto>([fileId]);
  }

  public upload(request: UploadMediaRequestDto): Promise<string[]> {
    const payload = new FormData();

    for (const file of request.files) {
      payload.append('files', file);
    }

    for (const albumId of request.albumIds ?? []) {
      payload.append('albumIds', albumId);
    }

    return firstValueFrom(
      this.http.post<string[]>(this.createUrl(), payload)
    );
  }

  public deleteById(fileId: string): Promise<void> {
    return this.delete<void>([fileId]);
  }

}
