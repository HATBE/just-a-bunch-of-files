package ch.hatbe.jbof.album;

import ch.hatbe.jbof.jooq.tables.records.AlbumsRecord;
import lombok.RequiredArgsConstructor;
import org.jooq.DSLContext;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static ch.hatbe.jbof.jooq.Tables.ALBUMS;

@Repository
@RequiredArgsConstructor
public class AlbumRepository {
    private final DSLContext dsl;

    public AlbumsRecord create(UUID ownerUserId, String name) {
        return dsl.insertInto(ALBUMS)
                .set(ALBUMS.OWNER_USER_ID, ownerUserId)
                .set(ALBUMS.NAME, name)
                .returning()
                .fetchOne();
    }

    public List<AlbumsRecord> findAll() {
        return dsl.selectFrom(ALBUMS)
                .orderBy(ALBUMS.CREATED_AT.desc(), ALBUMS.ALBUM_ID.asc())
                .fetch();
    }

    public Optional<AlbumsRecord> findById(UUID albumId) {
        return dsl.selectFrom(ALBUMS)
                .where(ALBUMS.ALBUM_ID.eq(albumId))
                .fetchOptional();
    }

    public Optional<AlbumsRecord> rename(UUID albumId, String name) {
        return dsl.update(ALBUMS)
                .set(ALBUMS.NAME, name)
                .where(ALBUMS.ALBUM_ID.eq(albumId))
                .returning()
                .fetchOptional();
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
}
