package ch.hatbe.jbof.user.entity;

import java.time.OffsetDateTime;
import java.util.UUID;

public record UserDetailDto(
        UUID userId,
        String username,
        OffsetDateTime createdAt
) {}
