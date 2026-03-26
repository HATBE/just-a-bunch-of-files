package ch.hatbe.jbof.media;

import ch.hatbe.jbof.jooq.tables.records.MediaFilesRecord;
import ch.hatbe.jbof.media.entity.MediaDtos;
import ch.hatbe.jbof.media.entity.MediaKind;
import ch.hatbe.jbof.user.entity.UserDtos;

import java.util.List;

public final class MediaMapper {
    private MediaMapper() {
    }

    public static MediaDtos.ListResponse toListResponse(MediaFilesRecord record, UserDtos.ListResponse user) {
        return new MediaDtos.ListResponse(
                record.getFileId(),
                user,
                MediaKind.valueOf(record.getKind()),
                record.getOriginalFilename(),
                record.getBucket(),
                record.getObjectKey(),
                record.getContentType(),
                record.getSizeBytes(),
                record.getUploadedAt()
        );
    }

    public static MediaDtos.DetailResponse toDetailResponse(
            MediaFilesRecord record,
            UserDtos.ListResponse user,
            List<MediaDtos.AlbumReference> albums
    ) {
        return new MediaDtos.DetailResponse(
                record.getFileId(),
                user,
                MediaKind.valueOf(record.getKind()),
                record.getOriginalFilename(),
                record.getBucket(),
                record.getObjectKey(),
                record.getContentType(),
                record.getSizeBytes(),
                record.getUploadedAt(),
                albums
        );
    }
}
