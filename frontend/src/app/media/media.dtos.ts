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
  uploadedAt: string;
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
  uploadedAt: string;
  albums: MediaAlbumReferenceDto[];
}

export interface UploadMediaRequestDto {
  userId: string;
  albumIds?: string[];
  file: File;
}
