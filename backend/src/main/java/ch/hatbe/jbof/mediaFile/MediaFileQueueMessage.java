package ch.hatbe.jbof.mediaFile;

import java.time.OffsetDateTime;
import java.util.UUID;

public record MediaFileQueueMessage(
        UUID mediaFileId,
        OffsetDateTime createdAt
) {}
