package ch.hatbe.jbof.album;

import ch.hatbe.jbof.album.entity.AlbumDtos;
import org.springframework.stereotype.Component;

@Component
public class AlbumMapper {
    public AlbumDtos.ListResponse toListResponse(AlbumDtos.ListView view) {
        return new AlbumDtos.ListResponse(
                view.albumId(),
                view.user(),
                view.name(),
                view.createdAt()
        );
    }

    public AlbumDtos.DetailResponse toDetailResponse(AlbumDtos.DetailView view) {
        return new AlbumDtos.DetailResponse(
                view.albumId(),
                view.user(),
                view.name(),
                view.createdAt(),
                view.files()
        );
    }
}
