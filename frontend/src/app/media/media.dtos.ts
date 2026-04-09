import { UserListResponseDto } from '../user/user.dtos';

export type MediaKindDto = 'IMAGE' | 'VIDEO';

export interface MediaAlbumReferenceDto {
  albumId: string;
  name: string;
}

export interface MediaListResponseDto {
  mediaFileId: string;
  owner: UserListResponseDto;
  kind: MediaKindDto;
  processingStatus: string;
  originalFilename: string;
  contentType: string;
}

export interface PageResponseDto<T> {
  content: T[];
  number: number;
  size: number;
  totalPages: number;
  totalElements: number;
  last: boolean;
}

export interface MediaDetailResponseDto {
  mediaFileId: string;
  owner: UserListResponseDto;
  kind: MediaKindDto;
  processingStatus: string;
  originalFilename: string;
  bucket: string;
  objectKey: string;
  contentType: string;
  albums: MediaAlbumReferenceDto[];
}

export interface UploadMediaRequestDto {
  albumIds?: string[];
  files: File[];
}
