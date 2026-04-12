package ch.hatbe.jbof.mediaFile.entity;

import java.time.OffsetDateTime;
import java.util.UUID;

public record MediaFileQueueMessage(
        UUID mediaFileId,
        OffsetDateTime createdAt
) {}
