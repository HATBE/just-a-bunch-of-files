package ch.hatbe;

import java.time.OffsetDateTime;

public record File (
         String path,
         String name,
         String type,
         String hash,
         OffsetDateTime date
) {}
