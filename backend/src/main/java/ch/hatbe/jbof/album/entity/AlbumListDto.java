package ch.hatbe.jbof.album.entity;

import ch.hatbe.jbof.user.entity.UserListDto;

import java.time.OffsetDateTime;
import java.util.UUID;

public record AlbumListDto(
        UUID albumId,
        String name,
        OffsetDateTime createdAt,
        UserListDto owner
) {}