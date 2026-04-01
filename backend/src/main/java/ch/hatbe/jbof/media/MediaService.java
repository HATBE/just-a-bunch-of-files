package ch.hatbe.jbof.media;

import ch.hatbe.jbof.album.AlbumRepository;
import ch.hatbe.jbof.core.exception.NotFoundException;
import ch.hatbe.jbof.media.entity.MediaDtos;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class MediaService {
    private final MediaRepository mediaRepository;
    private final AlbumRepository albumRepository;

    public List<MediaDtos.MediaFileListResponse> getAllForList() {
        return mediaRepository.getAll().stream()
                .map(this::toListResponse)
                .toList();
    }

    public List<MediaDtos.MediaFileListResponse> getAllForAlbum(UUID albumId) {
        if (!albumRepository.existsById(albumId)) {
            throw new NotFoundException("album not found");
        }

        return mediaRepository.getAllByAlbumId(albumId).stream()
                .map(this::toListResponse)
                .toList();
    }

    public MediaDtos.MediaFileDetailedResponse getDetailedById(UUID mediaFileId) {
        MediaDtos.MediaFileView mediaFile = mediaRepository.getById(mediaFileId)
                .orElseThrow(() -> new NotFoundException("media file not found"));

        return this.toDetailedResponse(mediaFile);
    }

    public void addToAlbum(UUID albumId, UUID mediaFileId) {
        MediaDtos.MediaFileView mediaFile = mediaRepository.getById(mediaFileId)
                .orElseThrow(() -> new NotFoundException("media file not found"));

        if (!albumRepository.existsByIdAndOwnerUserId(albumId, mediaFile.user().userId())) {
            throw new NotFoundException("album not found");
        }

        mediaRepository.addToAlbum(albumId, mediaFileId);
    }

    public void removeFromAlbum(UUID albumId, UUID mediaFileId) {
        if (!mediaRepository.existsById(mediaFileId)) {
            throw new NotFoundException("media file not found");
        }

        if (!albumRepository.existsById(albumId)) {
            throw new NotFoundException("album not found");
        }

        mediaRepository.removeFromAlbum(albumId, mediaFileId);
    }

    private MediaDtos.MediaFileListResponse toListResponse(MediaDtos.MediaFileView mediaFile) {
        return new MediaDtos.MediaFileListResponse(
                mediaFile.mediaFileId(),
                mediaFile.originalFilename(),
                mediaFile.contentType(),
                mediaFile.kind(),
                mediaFile.processingStatus(),
                mediaFile.uploadedAt(),
                mediaFile.user().username(),
                mediaFile.albums().stream()
                        .map(album -> new MediaDtos.MediaFileListResponse.AlbumItem(
                                album.albumId(),
                                album.name()
                        ))
                        .toList()
        );
    }

    private MediaDtos.MediaFileDetailedResponse toDetailedResponse(MediaDtos.MediaFileView mediaFile) {
        return new MediaDtos.MediaFileDetailedResponse(
                mediaFile.mediaFileId(),
                new MediaDtos.MediaFileDetailedResponse.UserItem(
                        mediaFile.user().userId(),
                        mediaFile.user().username()
                ),
                mediaFile.albums().stream()
                        .map(album -> new MediaDtos.MediaFileDetailedResponse.AlbumItem(
                                album.albumId(),
                                album.name(),
                                album.createdAt()
                        ))
                        .toList(),
                mediaFile.kind(),
                mediaFile.processingStatus(),
                mediaFile.originalFilename(),
                mediaFile.contentType(),
                mediaFile.sizeBytes(),
                mediaFile.capturedAt(),
                mediaFile.uploadedAt(),
                mediaFile.width(),
                mediaFile.height(),
                mediaFile.durationMs(),
                mediaFile.createdAt()
        );
    }
}
