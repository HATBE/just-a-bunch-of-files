package ch.hatbe.jbof.mediaFile.entity;

import jakarta.annotation.Nullable;
import jakarta.validation.constraints.NotNull;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

public record CreateMediaFileRequest(
        @NotNull(message = "ownerUserId must not be empty")
        UUID ownerUserId,

        @NotNull(message = "files must not be empty")
        List<MultipartFile> files,

        @Nullable
        List<UUID> albumIds
) {}
