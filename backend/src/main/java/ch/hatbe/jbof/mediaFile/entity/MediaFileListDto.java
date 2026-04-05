package ch.hatbe.jbof.mediaFile.entity;

import ch.hatbe.jbof.user.entity.UserListDto;

import java.time.OffsetDateTime;
import java.util.UUID;

public record MediaFileListDto(
        UUID mediaFileId,
        MediaKind kind,
        MediaProcessingStatus processingStatus,
        String originalFilename,
        String contentType,
        OffsetDateTime uploadedAt,
        UserListDto owner
) {}