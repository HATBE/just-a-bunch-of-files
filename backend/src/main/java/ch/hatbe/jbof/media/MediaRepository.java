package ch.hatbe.jbof.media;

import ch.hatbe.jbof.jooq.tables.records.MediaFilesRecord;
import ch.hatbe.jbof.media.entity.MediaProcessingStatus;
import lombok.RequiredArgsConstructor;
import org.jooq.DSLContext;
import org.jooq.Record2;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static ch.hatbe.jbof.jooq.Tables.ALBUMS;
import static ch.hatbe.jbof.jooq.Tables.ALBUM_MEDIA_FILES;
import static ch.hatbe.jbof.jooq.Tables.MEDIA_FILES;

@Repository
@RequiredArgsConstructor
public class MediaRepository {
    private final DSLContext dsl;

    public MediaFilesRecord create(
            UUID ownerUserId,
            String kind,
            String bucket,
            String objectKey,
            String originalFilename,
            String contentType,
            long sizeBytes
    ) {
        return dsl.insertInto(MEDIA_FILES)
            .set(MEDIA_FILES.OWNER_USER_ID, ownerUserId)
            .set(MEDIA_FILES.KIND, kind)
            .set(MEDIA_FILES.BUCKET, bucket)
            .set(MEDIA_FILES.OBJECT_KEY, objectKey)
            .set(MEDIA_FILES.ORIGINAL_FILENAME, originalFilename)
            .set(MEDIA_FILES.CONTENT_TYPE, contentType)
            .set(MEDIA_FILES.SIZE_BYTES, sizeBytes)
            .set(MEDIA_FILES.PROCESSING_STATUS, MediaProcessingStatus.UPLOADED.name())
            .returning()
            .fetchOne();
    }

    public List<MediaFilesRecord> findAll(UUID userId) {
        var query = dsl.selectFrom(MEDIA_FILES).where(MEDIA_FILES.PROCESSING_STATUS.eq(MediaProcessingStatus.UPLOADED.name())); // TODO: remove uplaoded and change to PROCESSED

        if (userId != null) {
            return query.and(MEDIA_FILES.OWNER_USER_ID.eq(userId))
                .orderBy(MEDIA_FILES.UPLOADED_AT.desc(), MEDIA_FILES.FILE_ID.asc())
                .fetch();
        }

        return query.orderBy(MEDIA_FILES.UPLOADED_AT.desc(), MEDIA_FILES.FILE_ID.asc())
                .fetch();
    }

    public Optional<MediaFilesRecord> findById(UUID fileId) {
        return dsl.selectFrom(MEDIA_FILES)
            .where(MEDIA_FILES.FILE_ID.eq(fileId))
            .and(MEDIA_FILES.PROCESSING_STATUS.eq(MediaProcessingStatus.UPLOADED.name())) // TODO: remove uplaoded and change to PROCESSED
            .fetchOptional();
    }

    public List<MediaFilesRecord> findByAlbumId(UUID albumId) {
        return dsl.select(MEDIA_FILES.fields())
            .from(MEDIA_FILES)
            .join(ALBUM_MEDIA_FILES).on(ALBUM_MEDIA_FILES.FILE_ID.eq(MEDIA_FILES.FILE_ID))
            .where(ALBUM_MEDIA_FILES.ALBUM_ID.eq(albumId))
            .and(MEDIA_FILES.PROCESSING_STATUS.eq(MediaProcessingStatus.UPLOADED.name())) // TODO: remove uplaoded and change to PROCESSED
            .orderBy(MEDIA_FILES.UPLOADED_AT.desc(), MEDIA_FILES.FILE_ID.asc())
            .fetchInto(MEDIA_FILES);
    }

    public void addToAlbum(UUID albumId, UUID fileId) {
        dsl.insertInto(ALBUM_MEDIA_FILES)
            .set(ALBUM_MEDIA_FILES.ALBUM_ID, albumId)
            .set(ALBUM_MEDIA_FILES.FILE_ID, fileId)
            .onConflictDoNothing()
            .execute();
    }

    public void removeFromAlbum(UUID albumId, UUID fileId) {
        dsl.deleteFrom(ALBUM_MEDIA_FILES)
            .where(ALBUM_MEDIA_FILES.ALBUM_ID.eq(albumId))
            .and(ALBUM_MEDIA_FILES.FILE_ID.eq(fileId))
            .execute();
    }

    public List<Record2<UUID, String>> findAlbumsForFile(UUID fileId) {
        return dsl.select(ALBUMS.ALBUM_ID, ALBUMS.NAME)
            .from(ALBUMS)
            .join(ALBUM_MEDIA_FILES).on(ALBUM_MEDIA_FILES.ALBUM_ID.eq(ALBUMS.ALBUM_ID))
            .where(ALBUM_MEDIA_FILES.FILE_ID.eq(fileId))
            .orderBy(ALBUMS.CREATED_AT.desc(), ALBUMS.ALBUM_ID.asc())
            .fetch();
    }

    public void deleteById(UUID fileId) {
        dsl.deleteFrom(MEDIA_FILES)
            .where(MEDIA_FILES.FILE_ID.eq(fileId))
            .execute();
    }
}