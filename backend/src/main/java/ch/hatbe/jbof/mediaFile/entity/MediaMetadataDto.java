package ch.hatbe.jbof.mediaFile.entity;

import java.time.OffsetDateTime;

public record MediaMetadataDto(
        Double gpsLat,
        Double gpsLon,
        String cameraMake,
        String cameraModel,
        Long sizeBytes,
        String checksumSha256,
        OffsetDateTime capturedAt,
        OffsetDateTime uploadedAt,
        Integer width,
        Integer height,
        Long durationMs,
        String metadataJson,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {}
