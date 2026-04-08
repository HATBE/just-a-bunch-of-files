package ch.hatbe.jbof.mediaFile.entity.dto;

import ch.hatbe.jbof.album.entity.dto.AlbumListDto;
import ch.hatbe.jbof.mediaFile.entity.MediaKind;
import ch.hatbe.jbof.mediaFile.entity.MediaProcessingStatus;
import ch.hatbe.jbof.user.entity.dto.UserListDto;

import java.util.List;
import java.util.UUID;

public record MediaFileDetailDto(
        UUID mediaFileId,
        MediaKind kind,
        MediaProcessingStatus processingStatus,
        String bucket,
        String objectKey,
        String originalFilename,
        String contentType,
        UserListDto owner,
        MediaMetadataDto metadata,
        List<MediaDerivativeDto> derivatives,
        List<AlbumListDto> albums
) {}
