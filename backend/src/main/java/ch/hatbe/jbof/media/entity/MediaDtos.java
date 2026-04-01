package ch.hatbe.jbof.media.entity;

import ch.hatbe.jbof.album.entity.AlbumDtos;
import ch.hatbe.jbof.user.entity.UserDtos;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

public class MediaDtos {
    public record MediaFileView(
            UUID mediaFileId,
            UserDtos.UserView user,
            List<AlbumDtos.AlbumView> albums,
            String kind,
            String processingStatus,
            String originalFilename,
            String contentType,
            Long sizeBytes,
            OffsetDateTime capturedAt,
            OffsetDateTime uploadedAt,
            Integer width,
            Integer height,
            Long durationMs,
            OffsetDateTime createdAt,
            String bucket,
            String objectKey,
            String checksumSha256
    ) {
    }

    public record MediaFileListResponse(
            UUID mediaFileId,
            String originalFilename,
            String contentType,
            String kind,
            String processingStatus,
            OffsetDateTime uploadedAt,
            String username,
            List<AlbumItem> albums
    ) {
        public record AlbumItem(
                UUID albumId,
                String name
        ) {
        }
    }

    public record MediaFileDetailedResponse(
            UUID mediaFileId,
            UserItem user,
            List<AlbumItem> albums,
            String kind,
            String processingStatus,
            String originalFilename,
            String contentType,
            Long sizeBytes,
            OffsetDateTime capturedAt,
            OffsetDateTime uploadedAt,
            Integer width,
            Integer height,
            Long durationMs,
            OffsetDateTime createdAt
    ) {
        public record UserItem(
                UUID userId,
                String username
        ) {
        }

        public record AlbumItem(
                UUID albumId,
                String name,
                OffsetDateTime createdAt
        ) {
        }
    }
}
