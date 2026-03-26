package ch.hatbe.jbof.media;

import ch.hatbe.jbof.jooq.tables.records.MediaFilesRecord;
import ch.hatbe.jbof.media.entity.MediaDtos;
import ch.hatbe.jbof.media.entity.MediaKind;
import ch.hatbe.jbof.user.entity.UserDtos;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface MediaMapper {
    MediaDtos.ListResponse toListResponse(MediaFilesRecord record, UserDtos.ListResponse user);

    MediaDtos.DetailResponse toDetailResponse(
            MediaFilesRecord record,
            UserDtos.ListResponse user,
            List<MediaDtos.AlbumReference> albums
    );

    default MediaKind map(String kind) {
        return kind == null ? null : MediaKind.valueOf(kind);
    }
}
