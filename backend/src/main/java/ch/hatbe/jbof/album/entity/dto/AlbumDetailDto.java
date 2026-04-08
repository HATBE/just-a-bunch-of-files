package ch.hatbe.jbof.album.entity.dto;

import ch.hatbe.jbof.mediaFile.entity.dto.MediaFileListDto;
import ch.hatbe.jbof.user.entity.dto.UserListDto;

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