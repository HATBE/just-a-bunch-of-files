package ch.hatbe.jbof.mediaFile.entity;

import ch.hatbe.jbof.album.entity.AlbumListDto;
import ch.hatbe.jbof.user.entity.UserListDto;

import java.time.OffsetDateTime;
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
        Long sizeBytes,
        String checksumSha256,
        OffsetDateTime capturedAt,
        OffsetDateTime uploadedAt,
        Integer width,
        Integer height,
        Long durationMs,
        OffsetDateTime createdAt,
        UserListDto owner,
        MediaMetadataDto metadata,
        List<MediaDerivativeDto> derivatives,
        List<AlbumListDto> albums
) {}