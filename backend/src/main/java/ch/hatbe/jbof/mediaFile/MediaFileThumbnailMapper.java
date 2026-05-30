package ch.hatbe.jbof.mediaFile;

import ch.hatbe.jbof.mediaFile.entity.MediaFileThumbnail;
import ch.hatbe.jbof.mediaFile.entity.dto.MediaFileThumbnailDto;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface MediaFileThumbnailMapper {
    MediaFileThumbnailDto toDto(MediaFileThumbnail thumbnail);
}