package ch.hatbe.jbof.mediaFile;

import ch.hatbe.jbof.mediaFile.entity.MediaFileMetadata;
import ch.hatbe.jbof.mediaFile.entity.dto.MediaFileMetadataDetailDto;
import ch.hatbe.jbof.mediaFile.entity.dto.MediaFileMetadataListDto;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface MediaFileMetadataMapper {
    MediaFileMetadataListDto toListDto(MediaFileMetadata metadata);
    MediaFileMetadataDetailDto toDetailDto(MediaFileMetadata metadata);
}