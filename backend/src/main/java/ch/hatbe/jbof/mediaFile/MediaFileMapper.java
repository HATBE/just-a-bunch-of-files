package ch.hatbe.jbof.mediaFile;


import ch.hatbe.jbof.album.AlbumMapper;
import ch.hatbe.jbof.mediaFile.entity.*;
import ch.hatbe.jbof.user.UserMapper;

public final class MediaFileMapper {
    public static MediaFileListDto toListDto(MediaFile entity) {
        return new MediaFileListDto(
                entity.getMediaFileId(),
                entity.getKind(),
                entity.getProcessingStatus(),
                entity.getOriginalFilename(),
                entity.getContentType(),
                entity.getUploadedAt(),
                UserMapper.toListDto(entity.getOwner())
        );
    }

    public static MediaFileListDto toListDtoWithoutOwnerLoop(MediaFile entity) {
        return new MediaFileListDto(
                entity.getMediaFileId(),
                entity.getKind(),
                entity.getProcessingStatus(),
                entity.getOriginalFilename(),
                entity.getContentType(),
                entity.getUploadedAt(),
                null
        );
    }

    public static MediaFileDetailDto toDetailDto(MediaFile entity) {
        return new MediaFileDetailDto(
                entity.getMediaFileId(),
                entity.getKind(),
                entity.getProcessingStatus(),
                entity.getBucket(),
                entity.getObjectKey(),
                entity.getOriginalFilename(),
                entity.getContentType(),
                entity.getSizeBytes(),
                entity.getChecksumSha256(),
                entity.getCapturedAt(),
                entity.getUploadedAt(),
                entity.getWidth(),
                entity.getHeight(),
                entity.getDurationMs(),
                entity.getCreatedAt(),
                UserMapper.toListDto(entity.getOwner()),
                toMetadataDto(entity.getMetadata()),
                entity.getDerivatives().stream().map(DerivativeMapper::toDto).toList(),
                entity.getAlbums().stream().map(AlbumMapper::toListDto).toList()
        );
    }

    private static MediaMetadataDto toMetadataDto(MediaMetadata entity) {
        if (entity == null) {
            return null;
        }

        return new MediaMetadataDto(
                entity.getGpsLat(),
                entity.getGpsLon(),
                entity.getCameraMake(),
                entity.getCameraModel(),
                entity.getMetadataJson(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }
}
