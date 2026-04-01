package ch.hatbe.jbof.media;

import ch.hatbe.jbof.album.entity.AlbumDtos;
import ch.hatbe.jbof.jooq.tables.AlbumMediaFiles;
import ch.hatbe.jbof.jooq.tables.Albums;
import ch.hatbe.jbof.jooq.tables.MediaFiles;
import ch.hatbe.jbof.jooq.tables.Users;
import ch.hatbe.jbof.user.UserViewFactory;
import ch.hatbe.jbof.media.entity.MediaDtos;
import lombok.RequiredArgsConstructor;
import org.jooq.DSLContext;
import org.jooq.Record;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.jooq.impl.DSL.multiset;
import static org.jooq.impl.DSL.select;

@Repository
@RequiredArgsConstructor
public class MediaRepository {
    private final DSLContext dsl;
    private final UserViewFactory userViewFactory;

    public List<MediaDtos.MediaFileView> getAll() {
        Users u = Users.USERS;
        MediaFiles mf = MediaFiles.MEDIA_FILES;
        Albums a = Albums.ALBUMS;
        AlbumMediaFiles amf = AlbumMediaFiles.ALBUM_MEDIA_FILES;

        return dsl.select(
                        mf.MEDIA_FILE_ID,
                        mf.KIND,
                        mf.PROCESSING_STATUS,
                        mf.ORIGINAL_FILENAME,
                        mf.CONTENT_TYPE,
                        mf.SIZE_BYTES,
                        mf.CAPTURED_AT,
                        mf.UPLOADED_AT,
                        mf.WIDTH,
                        mf.HEIGHT,
                        mf.DURATION_MS,
                        mf.CREATED_AT,
                        mf.BUCKET,
                        mf.OBJECT_KEY,
                        mf.CHECKSUM_SHA256,
                        u.USER_ID,
                        u.USERNAME,
                        u.CREATED_AT,
                        multiset(
                                select(
                                        a.ALBUM_ID,
                                        a.OWNER_USER_ID,
                                        a.NAME,
                                        a.CREATED_AT
                                )
                                        .from(amf)
                                        .join(a).on(a.ALBUM_ID.eq(amf.ALBUM_ID))
                                        .where(amf.MEDIA_FILE_ID.eq(mf.MEDIA_FILE_ID))
                        ).as("albums")
                )
                .from(mf)
                .join(u).on(u.USER_ID.eq(mf.OWNER_USER_ID))
                .orderBy(mf.UPLOADED_AT.desc(), mf.MEDIA_FILE_ID.asc())
                .fetch(this::mapMediaFile);
    }

    public List<MediaDtos.MediaFileView> getAllByAlbumId(UUID albumId) {
        Users u = Users.USERS;
        MediaFiles mf = MediaFiles.MEDIA_FILES;
        Albums a = Albums.ALBUMS;
        AlbumMediaFiles amf = AlbumMediaFiles.ALBUM_MEDIA_FILES;

        return dsl.select(
                        mf.MEDIA_FILE_ID,
                        mf.KIND,
                        mf.PROCESSING_STATUS,
                        mf.ORIGINAL_FILENAME,
                        mf.CONTENT_TYPE,
                        mf.SIZE_BYTES,
                        mf.CAPTURED_AT,
                        mf.UPLOADED_AT,
                        mf.WIDTH,
                        mf.HEIGHT,
                        mf.DURATION_MS,
                        mf.CREATED_AT,
                        mf.BUCKET,
                        mf.OBJECT_KEY,
                        mf.CHECKSUM_SHA256,
                        u.USER_ID,
                        u.USERNAME,
                        u.CREATED_AT,
                        multiset(
                                select(
                                        a.ALBUM_ID,
                                        a.OWNER_USER_ID,
                                        a.NAME,
                                        a.CREATED_AT
                                )
                                        .from(amf)
                                        .join(a).on(a.ALBUM_ID.eq(amf.ALBUM_ID))
                                        .where(amf.MEDIA_FILE_ID.eq(mf.MEDIA_FILE_ID))
                        ).as("albums")
                )
                .from(mf)
                .join(u).on(u.USER_ID.eq(mf.OWNER_USER_ID))
                .join(amf).on(amf.MEDIA_FILE_ID.eq(mf.MEDIA_FILE_ID))
                .where(amf.ALBUM_ID.eq(albumId))
                .orderBy(mf.UPLOADED_AT.desc(), mf.MEDIA_FILE_ID.asc())
                .fetch(this::mapMediaFile);
    }

    public Optional<MediaDtos.MediaFileView> getById(UUID mediaFileId) {
        Users u = Users.USERS;
        MediaFiles mf = MediaFiles.MEDIA_FILES;
        Albums a = Albums.ALBUMS;
        AlbumMediaFiles amf = AlbumMediaFiles.ALBUM_MEDIA_FILES;

        return dsl.select(
                        mf.MEDIA_FILE_ID,
                        mf.KIND,
                        mf.PROCESSING_STATUS,
                        mf.ORIGINAL_FILENAME,
                        mf.CONTENT_TYPE,
                        mf.SIZE_BYTES,
                        mf.CAPTURED_AT,
                        mf.UPLOADED_AT,
                        mf.WIDTH,
                        mf.HEIGHT,
                        mf.DURATION_MS,
                        mf.CREATED_AT,
                        mf.BUCKET,
                        mf.OBJECT_KEY,
                        mf.CHECKSUM_SHA256,
                        u.USER_ID,
                        u.USERNAME,
                        u.CREATED_AT,
                        multiset(
                                select(
                                        a.ALBUM_ID,
                                        a.OWNER_USER_ID,
                                        a.NAME,
                                        a.CREATED_AT
                                )
                                        .from(amf)
                                        .join(a).on(a.ALBUM_ID.eq(amf.ALBUM_ID))
                                        .where(amf.MEDIA_FILE_ID.eq(mf.MEDIA_FILE_ID))
                        ).as("albums")
                )
                .from(mf)
                .join(u).on(u.USER_ID.eq(mf.OWNER_USER_ID))
                .where(mf.MEDIA_FILE_ID.eq(mediaFileId))
                .fetchOptional(this::mapMediaFile);
    }

    public boolean existsById(UUID mediaFileId) {
        MediaFiles mf = MediaFiles.MEDIA_FILES;

        return dsl.fetchExists(
                dsl.selectOne()
                        .from(mf)
                        .where(mf.MEDIA_FILE_ID.eq(mediaFileId))
        );
    }

    public void addToAlbum(UUID albumId, UUID mediaFileId) {
        AlbumMediaFiles amf = AlbumMediaFiles.ALBUM_MEDIA_FILES;

        dsl.insertInto(amf)
                .set(amf.ALBUM_ID, albumId)
                .set(amf.MEDIA_FILE_ID, mediaFileId)
                .onConflictDoNothing()
                .execute();
    }

    public void removeFromAlbum(UUID albumId, UUID mediaFileId) {
        AlbumMediaFiles amf = AlbumMediaFiles.ALBUM_MEDIA_FILES;

        dsl.deleteFrom(amf)
                .where(amf.ALBUM_ID.eq(albumId))
                .and(amf.MEDIA_FILE_ID.eq(mediaFileId))
                .execute();
    }

    @SuppressWarnings("unchecked")
    private MediaDtos.MediaFileView mapMediaFile(Record record) {
        MediaFiles mf = MediaFiles.MEDIA_FILES;
        Albums a = Albums.ALBUMS;

        List<Record> albumRecords = (List<Record>) record.get("albums");
        List<AlbumDtos.AlbumView> albums = albumRecords.stream()
                .map(album -> new AlbumDtos.AlbumView(
                        album.get(a.ALBUM_ID),
                        album.get(a.OWNER_USER_ID),
                        album.get(a.NAME),
                        album.get(a.CREATED_AT)
                ))
                .toList();

        return new MediaDtos.MediaFileView(
                record.get(mf.MEDIA_FILE_ID),
                this.userViewFactory.fromJoinedRecord(record),
                albums,
                record.get(mf.KIND).name(),
                record.get(mf.PROCESSING_STATUS).name(),
                record.get(mf.ORIGINAL_FILENAME),
                record.get(mf.CONTENT_TYPE),
                record.get(mf.SIZE_BYTES),
                record.get(mf.CAPTURED_AT),
                record.get(mf.UPLOADED_AT),
                record.get(mf.WIDTH),
                record.get(mf.HEIGHT),
                record.get(mf.DURATION_MS),
                record.get(mf.CREATED_AT),
                record.get(mf.BUCKET),
                record.get(mf.OBJECT_KEY),
                record.get(mf.CHECKSUM_SHA256)
        );
    }
}
