package ch.hatbe.jbof.album;

import ch.hatbe.jbof.album.entity.AlbumDtos;
import ch.hatbe.jbof.jooq.tables.records.AlbumsRecord;
import ch.hatbe.jbof.media.entity.MediaDtos;
import ch.hatbe.jbof.user.entity.UserDtos;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface AlbumMapper {
    AlbumDtos.ListResponse toListResponse(AlbumsRecord record, UserDtos.ListResponse user);

    AlbumDtos.DetailResponse toDetailResponse(
            AlbumsRecord record,
            UserDtos.ListResponse user,
            List<MediaDtos.ListResponse> files
    );
}
