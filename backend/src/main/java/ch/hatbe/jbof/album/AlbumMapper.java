package ch.hatbe.jbof.album;

import ch.hatbe.jbof.album.entity.AlbumDtos;
import ch.hatbe.jbof.jooq.tables.records.AlbumsRecord;
import ch.hatbe.jbof.media.entity.MediaDtos;
import ch.hatbe.jbof.user.entity.UserDtos;

import java.util.List;

public final class AlbumMapper {
    private AlbumMapper() {
    }

    public static AlbumDtos.ListResponse toListResponse(AlbumsRecord record, UserDtos.ListResponse user) {
        return new AlbumDtos.ListResponse(
                record.getAlbumId(),
                user,
                record.getName(),
                record.getCreatedAt()
        );
    }

    public static AlbumDtos.DetailResponse toDetailResponse(
            AlbumsRecord record,
            UserDtos.ListResponse user,
            List<MediaDtos.ListResponse> files
    ) {
        return new AlbumDtos.DetailResponse(
                record.getAlbumId(),
                user,
                record.getName(),
                record.getCreatedAt(),
                files
        );
    }
}
