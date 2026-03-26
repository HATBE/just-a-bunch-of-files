package ch.hatbe.jbof.album.entity;

import ch.hatbe.jbof.media.entity.MediaDtos;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

public class AlbumDtos {
    public record CreateAlbumRequest(
            @NotNull UUID ownerUserId,
            @NotBlank String name
    ) {
    }

    public record ListResponse(
            UUID albumId,
            UUID ownerUserId,
            String name,
            OffsetDateTime createdAt
    ) {
    }

    public record DetailResponse(
            UUID albumId,
            UUID ownerUserId,
            String name,
            OffsetDateTime createdAt,
            List<MediaDtos.ListResponse> files
    ) {
    }
}
