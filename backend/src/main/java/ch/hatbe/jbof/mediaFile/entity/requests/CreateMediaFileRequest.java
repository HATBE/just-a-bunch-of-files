package ch.hatbe.jbof.mediaFile.entity.requests;

import jakarta.annotation.Nullable;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

public record CreateMediaFileRequest(
        @NotNull(message = "ownerUserId must not be empty")
        UUID ownerUserId,

        @NotNull(message = "files must not be empty")
        @Size(max = 100, min = 1, message = "there must be between 1 and 100 files")
        List<MultipartFile> files,

        @Nullable
        List<UUID> albumIds
) {}
