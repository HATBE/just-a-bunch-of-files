package ch.hatbe.jbof.mediaFile.entity.dto;

import ch.hatbe.jbof.mediaFile.entity.MediaDerivativeKind;

import java.time.OffsetDateTime;
import java.util.UUID;

public record MediaDerivativeDto(
        UUID derivativeId,
        MediaDerivativeKind kind,
        String bucket,
        String objectKey,
        String contentType,
        Integer width,
        Integer height,
        Long sizeBytes,
        OffsetDateTime createdAt
) {}
