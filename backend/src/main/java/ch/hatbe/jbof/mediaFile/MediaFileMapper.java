package ch.hatbe.jbof.mediaFile;

import ch.hatbe.jbof.album.AlbumMapper;
import ch.hatbe.jbof.mediaFile.entity.MediaFile;
import ch.hatbe.jbof.mediaFile.entity.dto.MediaFileDetailDto;
import ch.hatbe.jbof.mediaFile.entity.dto.MediaFileListDto;
import ch.hatbe.jbof.user.UserMapper;
import org.mapstruct.Mapper;

@Mapper(
        componentModel = "spring",
        uses = {
                UserMapper.class,
                MediaFileMetadataMapper.class,
                MediaFileThumbnailMapper.class,
                AlbumMapper.class
        }
)
public interface MediaFileMapper {
    MediaFileListDto toListDto(MediaFile mediaFile);
    MediaFileDetailDto toDetailDto(MediaFile mediaFile);
}