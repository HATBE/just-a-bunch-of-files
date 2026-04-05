package ch.hatbe.jbof.album.entity;

import ch.hatbe.jbof.mediaFile.entity.MediaFileListDto;
import ch.hatbe.jbof.user.entity.UserListDto;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

public record AlbumDetailDto(
        UUID albumId,
        String name,
        OffsetDateTime createdAt,
        UserListDto owner,
        List<MediaFileListDto> mediaFiles
) {}