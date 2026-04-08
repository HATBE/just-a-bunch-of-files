package ch.hatbe.jbof.album.entity.requests;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record CreateAlbumRequest(
        @NotNull UUID ownerUserId,
        @NotBlank String name
) {}
