package ch.hatbe.jbof.mediaFile.entity;

import java.time.OffsetDateTime;

public record MediaMetadataDto(
        Double gpsLat,
        Double gpsLon,
        String cameraMake,
        String cameraModel,
        String metadataJson,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {}
