package ch.hatbe.jbof.album;

import ch.hatbe.jbof.album.entity.Album;
import ch.hatbe.jbof.album.entity.dto.AlbumDetailDto;
import ch.hatbe.jbof.album.entity.dto.AlbumListDto;
import ch.hatbe.jbof.mediaFile.MediaFileMapper;
import ch.hatbe.jbof.mediaFile.entity.dto.MediaFileListDto;
import ch.hatbe.jbof.user.UserMapper;

import java.util.List;


public final class AlbumMapper {

    private AlbumMapper() {
    }

    public static AlbumListDto toListDto(Album entity) {
        return new AlbumListDto(
                entity.getAlbumId(),
                entity.getName(),
                entity.getCreatedAt(),
                UserMapper.toListDto(entity.getOwner()),
                entity.getMediaFileCount()
        );
    }

    public static AlbumDetailDto toDetailDto(Album entity) {
        List<MediaFileListDto> mediaFiles = entity.getMediaFiles().stream()
                .map(MediaFileMapper::toListDto)
                .toList();

        return new AlbumDetailDto(
                entity.getAlbumId(),
                entity.getName(),
                entity.getCreatedAt(),
                UserMapper.toListDto(entity.getOwner()),
                mediaFiles
        );
    }
}
