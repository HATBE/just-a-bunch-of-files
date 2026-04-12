package ch.hatbe.jbof.mediaFile.entity.dto;

import ch.hatbe.jbof.mediaFile.entity.MediaKind;
import ch.hatbe.jbof.mediaFile.entity.MediaProcessingStatus;
import ch.hatbe.jbof.user.entity.dto.UserListDto;

import java.time.OffsetDateTime;
import java.util.UUID;

public record MediaFileListDto(
        UUID mediaFileId,
        MediaKind kind,
        MediaProcessingStatus processingStatus,
        String originalFilename,
        String contentType,
        OffsetDateTime capturedAt,
        OffsetDateTime uploadedAt,
        UserListDto owner
) {}
