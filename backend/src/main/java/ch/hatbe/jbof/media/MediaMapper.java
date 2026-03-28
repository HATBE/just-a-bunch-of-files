package ch.hatbe.jbof.media;

import ch.hatbe.jbof.jooq.tables.records.MediaFilesRecord;
import ch.hatbe.jbof.media.entity.MediaDtos;
import ch.hatbe.jbof.media.entity.MediaKind;
import ch.hatbe.jbof.user.entity.UserDtos;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface MediaMapper {
    MediaDtos.DetailResponse toDetailResponse(
            MediaFilesRecord record,
            UserDtos.ListResponse user,
            List<MediaDtos.AlbumReference> albums
    );

    default MediaDtos.ListResponse toListResponse(MediaFilesRecord record, UserDtos.ListResponse user) {
        String bucket = record.getThumbnailBucket() == null || record.getThumbnailBucket().isBlank()
                ? record.getBucket()
                : record.getThumbnailBucket();
        String objectKey = record.getThumbnailObjectKey() == null || record.getThumbnailObjectKey().isBlank()
                ? record.getObjectKey()
                : record.getThumbnailObjectKey();
        String contentType = record.getThumbnailContentType() == null || record.getThumbnailContentType().isBlank()
                ? record.getContentType()
                : record.getThumbnailContentType();
        Long sizeBytes = record.getThumbnailSizeBytes() == null
                ? record.getSizeBytes()
                : record.getThumbnailSizeBytes();

        return new MediaDtos.ListResponse(
                record.getFileId(),
                user,
                map(record.getKind()),
                record.getOriginalFilename(),
                bucket,
                objectKey,
                contentType,
                sizeBytes,
                record.getCapturedAt(),
                record.getUploadedAt()
        );
    }

    default MediaKind map(String kind) {
        return kind == null ? null : MediaKind.valueOf(kind);
    }
}
