package ch.hatbe.jbof.album;

import ch.hatbe.jbof.album.entity.AlbumDetailDto;
import ch.hatbe.jbof.album.entity.AlbumListDto;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@Transactional
@RequiredArgsConstructor
public class AlbumService {
    private final AlbumRepository albumRepository;

    public List<AlbumListDto> findAll() {
        return albumRepository.findAllByOrderByCreatedAtDesc()
                .stream()
                .map(AlbumMapper::toListDto)
                .toList();
    }

    public Optional<AlbumDetailDto> findById(UUID id) {
        return albumRepository.findByAlbumId(id)
                .map(AlbumMapper::toDetailDto);
    }
}