package ch.hatbe.jbof.mediaFile.entity.dto;

import ch.hatbe.jbof.mediaFile.entity.MediaKind;
import ch.hatbe.jbof.mediaFile.entity.MediaProcessingStatus;
import ch.hatbe.jbof.user.entity.dto.UserListDto;

import java.util.UUID;

public record MediaFileListDto(
        UUID mediaFileId,
        MediaKind kind,
        MediaProcessingStatus processingStatus,
        String originalFilename,
        String contentType,
        UserListDto owner
) {}
