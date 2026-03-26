package ch.hatbe.jbof.media.entity;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

public class MediaDtos {
    public record AlbumReference(
            UUID albumId,
            String name
    ) { }

    public record ListResponse(
            UUID fileId,
            UUID ownerUserId,
            MediaKind kind,
            String originalFilename,
            String bucket,
            String objectKey,
            String contentType,
            Long sizeBytes,
            OffsetDateTime uploadedAt
    ) { }

    public record DetailResponse(
            UUID fileId,
            UUID ownerUserId,
            MediaKind kind,
            String originalFilename,
            String bucket,
            String objectKey,
            String contentType,
            Long sizeBytes,
            OffsetDateTime uploadedAt,
            List<AlbumReference> albums
    ) { }
}
