package ch.hatbe.jbof.album;

import ch.hatbe.jbof.album.entity.Album;
import ch.hatbe.jbof.album.entity.dto.AlbumDetailDto;
import ch.hatbe.jbof.album.entity.dto.AlbumListDto;
import ch.hatbe.jbof.album.entity.request.CreateAlbumRequest;
import ch.hatbe.jbof.auth.CurrentUser;
import ch.hatbe.jbof.core.exception.ResourceNotFoundException;
import ch.hatbe.jbof.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

// TODO: handle with admin permission
// TODO: handle getting with any user id when user has permission

@Service
@RequiredArgsConstructor
public class AlbumService {
    private final CurrentUser currentUser;
    private final AlbumRepository albumRepository;
    private final AlbumMapper albumMapper;

    public List<AlbumListDto> findAll(Pageable pageable) {
        return this.albumRepository.findAllByOwnerUserIdOrderByCreatedAtDesc(currentUser.get().getUserId(), pageable)
                .stream()
                .map(this.albumMapper::toListDto)
                .toList();
    }

    public Optional<AlbumDetailDto> findById(UUID id) {
        return this.albumRepository.findByAlbumIdAndOwnerUserId(id, currentUser.get().getUserId())
                .map(this.albumMapper::toDetailDto);
    }

    public UUID create(CreateAlbumRequest req) {
        Album album = new Album();

        album.setName(req.name());
        album.setOwner(this.currentUser.get());

        return this.albumRepository.save(album).getAlbumId();
    }

    public void delete(UUID id) {
        Album album = this.albumRepository.findByAlbumIdAndOwnerUserId(id, this.currentUser.get().getUserId())
                .orElseThrow(() -> new ResourceNotFoundException(String.format("Album with id \"%s\" not found", id)));

        this.albumRepository.delete(album);
    }
}
