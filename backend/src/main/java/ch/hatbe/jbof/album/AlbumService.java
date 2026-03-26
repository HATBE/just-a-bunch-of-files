package ch.hatbe.jbof.album;

import ch.hatbe.jbof.album.entity.AlbumDtos;
import ch.hatbe.jbof.core.exception.NotFoundException;
import ch.hatbe.jbof.jooq.tables.records.AlbumsRecord;
import ch.hatbe.jbof.media.MediaService;
import ch.hatbe.jbof.media.entity.MediaDtos;
import ch.hatbe.jbof.user.entity.UserDtos;
import ch.hatbe.jbof.user.UserRepository;
import ch.hatbe.jbof.user.UserMapper;
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
    private final UserMapper userMapper;

    public AlbumDtos.ListResponse create(AlbumDtos.CreateAlbumRequest request) {
        if (!userRepository.existsById(request.userId())) {
            throw new NotFoundException("user not found");
        }

        AlbumsRecord record = repository.create(request.userId(), request.name());
        return albumMapper.toListResponse(record, getUserSummary(record.getOwnerUserId()));
    }

    public List<AlbumDtos.ListResponse> findAll() {
        return repository.findAll()
                .stream()
                .map(record -> albumMapper.toListResponse(record, getUserSummary(record.getOwnerUserId())))
                .toList();
    }

    public AlbumDtos.DetailResponse findById(UUID albumId) {
        AlbumsRecord record = repository.findById(albumId)
                .orElseThrow(() -> new NotFoundException("album not found"));

        List<MediaDtos.ListResponse> files = mediaService.findByAlbumId(albumId);

        return albumMapper.toDetailResponse(record, getUserSummary(record.getOwnerUserId()), files);
    }

    public AlbumDtos.ListResponse rename(UUID albumId, AlbumDtos.RenameAlbumRequest request) {
        AlbumsRecord record = repository.rename(albumId, request.name())
                .orElseThrow(() -> new NotFoundException("album not found"));

        return albumMapper.toListResponse(record, getUserSummary(record.getOwnerUserId()));
    }

    public void addFile(UUID albumId, UUID fileId) {
        mediaService.addToAlbum(albumId, fileId);
    }

    public void removeFile(UUID albumId, UUID fileId) {
        mediaService.removeFromAlbum(albumId, fileId);
    }

    private UserDtos.ListResponse getUserSummary(UUID userId) {
        return userRepository.findById(userId)
                .map(userMapper::toListResponse)
                .orElseThrow(() -> new NotFoundException("user not found"));
    }
}
