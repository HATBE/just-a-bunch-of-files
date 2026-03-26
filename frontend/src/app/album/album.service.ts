import { Injectable } from '@angular/core';
import { HttpService } from '../core/http.service';
import {
  AlbumDetailResponseDto,
  AlbumListResponseDto,
  CreateAlbumRequestDto,
  RenameAlbumRequestDto
} from './album.dtos';

@Injectable({
  providedIn: 'root'
})
export class AlbumService extends HttpService {
  constructor() {
    super();
    this.setEntity('albums');
  }

  public getAll(): Promise<AlbumListResponseDto[]> {
    return this.get<AlbumListResponseDto[]>([]);
  }

  public getById(albumId: string): Promise<AlbumDetailResponseDto> {
    return this.get<AlbumDetailResponseDto>([albumId]);
  }

  public create(request: CreateAlbumRequestDto): Promise<AlbumListResponseDto> {
    return this.post<AlbumListResponseDto, CreateAlbumRequestDto>([], request);
  }

  public rename(albumId: string, request: RenameAlbumRequestDto): Promise<AlbumListResponseDto> {
    return this.patch<AlbumListResponseDto, RenameAlbumRequestDto>([albumId], request);
  }

  public addFile(albumId: string, fileId: string): Promise<void> {
    return this.post<void, Record<string, never>>([albumId, 'files', fileId], {});
  }

  public removeFile(albumId: string, fileId: string): Promise<void> {
    return this.delete<void>([albumId, 'files', fileId]);
  }
}
