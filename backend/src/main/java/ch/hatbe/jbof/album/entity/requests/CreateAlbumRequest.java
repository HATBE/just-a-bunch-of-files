package ch.hatbe.jbof.album.entity.requests;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.hibernate.validator.constraints.Length;

import java.util.UUID;

public record CreateAlbumRequest(
        @NotNull(message = "ownerUserId must not be empty")
        UUID ownerUserId,

        @NotBlank(message = "name must not be empty")
        @NotNull(message = "name must not be empty")
        @Size(max = 255, min = 0, message = "name must be between 0 and 255 characters!")
        String name
) {}
