package ch.hatbe.jbof.album;

import ch.hatbe.jbof.album.entity.AlbumDtos;
import ch.hatbe.jbof.core.exception.NotFoundException;
import ch.hatbe.jbof.media.MediaService;
import ch.hatbe.jbof.media.entity.MediaDtos;
import ch.hatbe.jbof.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AlbumService {
    private final AlbumRepository repository;
    private final UserRepository userRepository;
    private final MediaService mediaService;
    private final AlbumMapper albumMapper;

    public AlbumDtos.ListResponse create(AlbumDtos.CreateAlbumRequest request) {
        if (!userRepository.existsById(request.userId())) {
            throw new NotFoundException("user not found");
        }

        return albumMapper.toListResponse(repository.create(request.userId(), request.name()));
    }

    public List<AlbumDtos.ListResponse> findAll() {
        return repository.findAll()
                .stream()
                .map(albumMapper::toListResponse)
                .toList();
    }

    public AlbumDtos.DetailResponse findById(UUID albumId) {
        List<MediaDtos.MediaFileListResponse> files = mediaService.getAllForAlbum(albumId);

        return repository.findDetailById(albumId, files)
                .map(albumMapper::toDetailResponse)
                .orElseThrow(() -> new NotFoundException("album not found"));
    }

    public AlbumDtos.ListResponse rename(UUID albumId, AlbumDtos.RenameAlbumRequest request) {
        return repository.rename(albumId, request.name())
                .map(albumMapper::toListResponse)
                .orElseThrow(() -> new NotFoundException("album not found"));
    }

    public void addFile(UUID albumId, UUID fileId) {
        mediaService.addToAlbum(albumId, fileId);
    }

    public void removeFile(UUID albumId, UUID fileId) {
        mediaService.removeFromAlbum(albumId, fileId);
    }
}
