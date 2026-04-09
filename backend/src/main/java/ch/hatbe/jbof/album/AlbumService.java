package ch.hatbe.jbof.album;

import ch.hatbe.jbof.album.entity.Album;
import ch.hatbe.jbof.album.entity.dto.AlbumDetailDto;
import ch.hatbe.jbof.album.entity.dto.AlbumListDto;
import ch.hatbe.jbof.album.entity.requests.CreateAlbumRequest;
import ch.hatbe.jbof.core.security.AuthenticatedUserService;
import ch.hatbe.jbof.core.exception.NotFoundException;
import ch.hatbe.jbof.user.entity.User;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
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
    private final AuthenticatedUserService authenticatedUserService;

    public List<AlbumListDto> findAll(Pageable pageable) {
        User currentUser = this.authenticatedUserService.getCurrentUser();

        return this.albumRepository.findAllByOwnerUserIdOrderByCreatedAtDesc(currentUser.getUserId(), pageable)
                .stream()
                .map(AlbumMapper::toListDto)
                .toList();
    }

    public Optional<AlbumDetailDto> findById(UUID id) {
        User currentUser = this.authenticatedUserService.getCurrentUser();

        return this.albumRepository.findByAlbumIdAndOwnerUserId(id, currentUser.getUserId())
                .map(AlbumMapper::toDetailDto);
    }

    public UUID create(CreateAlbumRequest request) {
        User owner = this.authenticatedUserService.getCurrentUser();

        Album album = new Album();
        album.setOwner(owner);
        album.setName(request.name());
        album.setCreatedAt(OffsetDateTime.now());

        Album createdAlbum = this.albumRepository.save(album);

        return createdAlbum.getAlbumId();
    }

    public void delete(UUID id) {
        User currentUser = this.authenticatedUserService.getCurrentUser();

        Album album = this.albumRepository.findByAlbumIdAndOwnerUserId(id, currentUser.getUserId())
                .orElseThrow(() -> new NotFoundException("album not found: " + id));

        this.albumRepository.delete(album);
    }
}
