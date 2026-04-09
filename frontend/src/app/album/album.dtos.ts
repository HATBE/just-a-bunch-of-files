import { MediaListResponseDto } from '../media/media.dtos';
import { UserListResponseDto } from '../user/user.dtos';

export interface CreateAlbumRequestDto {
  name: string;
}

export interface RenameAlbumRequestDto {
  name: string;
}

export interface AlbumListResponseDto {
  albumId: string;
  owner: UserListResponseDto;
  name: string;
  createdAt: string;
  mediaFileCount: number;
}

export interface AlbumDetailResponseDto {
  albumId: string;
  owner: UserListResponseDto;
  name: string;
  createdAt: string;
  mediaFiles: MediaListResponseDto[];
}
