package ch.hatbe.jbof.album;

import ch.hatbe.jbof.album.entity.Album;
import ch.hatbe.jbof.album.entity.AlbumDetailDto;
import ch.hatbe.jbof.album.entity.AlbumListDto;
import ch.hatbe.jbof.mediaFile.MediaFileMapper;
import ch.hatbe.jbof.mediaFile.entity.MediaFileListDto;
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
                UserMapper.toListDto(entity.getOwner())
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
