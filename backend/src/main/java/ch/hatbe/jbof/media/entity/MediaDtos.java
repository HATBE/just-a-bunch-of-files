package ch.hatbe.jbof.media.entity;

import ch.hatbe.jbof.user.entity.UserDtos;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

public class MediaDtos {
    public record AlbumReference(
            UUID albumId,
            String name
    ) {}

    public record ListResponse(
            UUID fileId,
            UserDtos.ListResponse user,
            MediaKind kind,
            String originalFilename,
            String bucket,
            String objectKey,
            String contentType,
            Long sizeBytes,
            OffsetDateTime capturedAt,
            OffsetDateTime uploadedAt
    ) {}

    public record DetailResponse(
            UUID fileId,
            UserDtos.ListResponse user,
            MediaKind kind,
            String originalFilename,
            String bucket,
            String objectKey,
            String contentType,
            Long sizeBytes,
            OffsetDateTime capturedAt,
            OffsetDateTime uploadedAt,
            List<AlbumReference> albums
    ) {}

    public record MediaDownload(
            String originalFilename,
            String contentType,
            long sizeBytes,
            ResponseInputStream<GetObjectResponse> stream
    ) { }
}
