package ch.hatbe.jbof.album;

import ch.hatbe.jbof.album.entity.Album;
import ch.hatbe.jbof.album.entity.dto.AlbumDetailDto;
import ch.hatbe.jbof.album.entity.dto.AlbumListDto;
import ch.hatbe.jbof.album.entity.requests.CreateAlbumRequest;
import ch.hatbe.jbof.core.exception.NotFoundException;
import ch.hatbe.jbof.user.UserRepository;
import ch.hatbe.jbof.user.entity.User;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@Transactional
@RequiredArgsConstructor
public class AlbumService {
    private final AlbumRepository albumRepository;
    private final UserRepository userRepository;

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

    public AlbumDetailDto create(CreateAlbumRequest request) {
        User owner = userRepository.findByUserId(request.ownerUserId())
                .orElseThrow(() -> new NotFoundException("user not found: " + request.ownerUserId()));

        Album album = new Album();
        album.setOwner(owner);
        album.setName(request.name());
        album.setCreatedAt(OffsetDateTime.now());

        return AlbumMapper.toDetailDto(albumRepository.save(album));
    }

    public void delete(UUID id) {
        Album album = albumRepository.findByAlbumId(id)
                .orElseThrow(() -> new NotFoundException("album not found: " + id));

        albumRepository.delete(album);
    }
}
