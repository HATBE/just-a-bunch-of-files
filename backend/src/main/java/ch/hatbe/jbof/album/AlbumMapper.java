package ch.hatbe.jbof.album;

import ch.hatbe.jbof.album.entity.Album;
import ch.hatbe.jbof.album.entity.dto.AlbumDetailDto;
import ch.hatbe.jbof.album.entity.dto.AlbumListDto;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface AlbumMapper {
    AlbumListDto toListDto(Album album);
    AlbumDetailDto toDetailDto(Album album);
}
