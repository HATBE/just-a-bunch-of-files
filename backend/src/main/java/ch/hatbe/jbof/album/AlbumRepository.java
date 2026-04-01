package ch.hatbe.jbof.album;

import ch.hatbe.jbof.album.entity.AlbumDtos;
import ch.hatbe.jbof.jooq.tables.records.AlbumsRecord;
import ch.hatbe.jbof.media.entity.MediaDtos;
import ch.hatbe.jbof.user.UserViewFactory;
import lombok.RequiredArgsConstructor;
import org.jooq.DSLContext;
import org.jooq.Record;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static ch.hatbe.jbof.jooq.Tables.ALBUMS;
import static ch.hatbe.jbof.jooq.Tables.USERS;

@Repository
@RequiredArgsConstructor
public class AlbumRepository {
    private final DSLContext dsl;
    private final UserViewFactory userViewFactory;

    public AlbumDtos.ListView create(UUID ownerUserId, String name) {
        AlbumsRecord record = dsl.insertInto(ALBUMS)
                .set(ALBUMS.OWNER_USER_ID, ownerUserId)
                .set(ALBUMS.NAME, name)
                .returning()
                .fetchOne();
        assert record != null;
        return this.findById(record.getAlbumId())
                .orElseThrow(() -> new IllegalStateException("Created album could not be loaded"));
    }

    public List<AlbumDtos.ListView> findAll() {
        return dsl.select(ALBUMS.fields())
                .select(USERS.fields())
                .from(ALBUMS)
                .join(USERS).on(USERS.USER_ID.eq(ALBUMS.OWNER_USER_ID))
                .orderBy(ALBUMS.CREATED_AT.desc(), ALBUMS.ALBUM_ID.asc())
                .fetch(this::toListView);
    }

    public Optional<AlbumDtos.ListView> findById(UUID albumId) {
        return dsl.select(ALBUMS.fields())
                .select(USERS.fields())
                .from(ALBUMS)
                .join(USERS).on(USERS.USER_ID.eq(ALBUMS.OWNER_USER_ID))
                .where(ALBUMS.ALBUM_ID.eq(albumId))
                .fetchOptional(this::toListView);
    }

    public Optional<AlbumDtos.ListView> rename(UUID albumId, String name) {
        return dsl.update(ALBUMS)
                .set(ALBUMS.NAME, name)
                .where(ALBUMS.ALBUM_ID.eq(albumId))
                .returning()
                .fetchOptional(record -> this.findById(record.getAlbumId())
                        .orElseThrow(() -> new IllegalStateException("Renamed album could not be loaded")));
    }

    public Optional<AlbumDtos.DetailView> findDetailById(UUID albumId, List<MediaDtos.MediaFileListResponse> files) {
        return this.findById(albumId)
                .map(view -> new AlbumDtos.DetailView(
                        view.albumId(),
                        view.user(),
                        view.name(),
                        view.createdAt(),
                        files
                ));
    }

    public boolean existsById(UUID albumId) {
        return dsl.fetchExists(
                dsl.selectOne()
                        .from(ALBUMS)
                        .where(ALBUMS.ALBUM_ID.eq(albumId))
        );
    }

    public boolean existsByIdAndOwnerUserId(UUID albumId, UUID ownerUserId) {
        return dsl.fetchExists(
                dsl.selectOne()
                        .from(ALBUMS)
                        .where(ALBUMS.ALBUM_ID.eq(albumId))
                        .and(ALBUMS.OWNER_USER_ID.eq(ownerUserId))
        );
    }

    private AlbumDtos.ListView toListView(Record record) {
        return new AlbumDtos.ListView(
                record.get(ALBUMS.ALBUM_ID),
                this.userViewFactory.toListResponse(record),
                record.get(ALBUMS.NAME),
                record.get(ALBUMS.CREATED_AT)
        );
    }
}
