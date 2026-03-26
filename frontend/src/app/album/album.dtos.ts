import { MediaListResponseDto } from '../media/media.dtos';
import { UserListResponseDto } from '../user/user.dtos';

export interface CreateAlbumRequestDto {
  userId: string;
  name: string;
}

export interface RenameAlbumRequestDto {
  name: string;
}

export interface AlbumListResponseDto {
  albumId: string;
  user: UserListResponseDto;
  name: string;
  createdAt: string;
}

export interface AlbumDetailResponseDto {
  albumId: string;
  user: UserListResponseDto;
  name: string;
  createdAt: string;
  files: MediaListResponseDto[];
}
