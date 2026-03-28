import { UserListResponseDto } from '../user/user.dtos';

export type MediaKindDto = 'IMAGE' | 'VIDEO';

export interface MediaAlbumReferenceDto {
  albumId: string;
  name: string;
}

export interface MediaListResponseDto {
  fileId: string;
  user: UserListResponseDto;
  kind: MediaKindDto;
  originalFilename: string;
  bucket: string;
  objectKey: string;
  contentType: string;
  sizeBytes: number;
  capturedAt: string | null;
  uploadedAt: string;
}

export interface MediaPagedResponseDto {
  items: MediaListResponseDto[];
  limit: number;
  offset: number;
  hasMore: boolean;
}

export interface MediaDetailResponseDto {
  fileId: string;
  user: UserListResponseDto;
  kind: MediaKindDto;
  originalFilename: string;
  bucket: string;
  objectKey: string;
  contentType: string;
  sizeBytes: number;
  capturedAt: string | null;
  uploadedAt: string;
  albums: MediaAlbumReferenceDto[];
}

export interface UploadMediaRequestDto {
  userId: string;
  albumIds?: string[];
  files: File[];
}
