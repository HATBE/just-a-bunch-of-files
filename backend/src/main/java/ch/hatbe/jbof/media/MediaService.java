package ch.hatbe.jbof.media;

import ch.hatbe.jbof.album.AlbumRepository;
import ch.hatbe.jbof.core.exception.NotFoundException;
import ch.hatbe.jbof.jooq.tables.records.MediaFilesRecord;
import ch.hatbe.jbof.media.entity.MediaDtos;
import ch.hatbe.jbof.media.entity.MediaKind;
import ch.hatbe.jbof.storage.StorageService;
import ch.hatbe.jbof.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class MediaService {
    private static final String IMAGE_BUCKET = "images";
    private static final String VIDEO_BUCKET = "videos";

    private final MediaRepository repository;
    private final UserRepository userRepository;
    private final AlbumRepository albumRepository;
    private final StorageService storageService;

    public MediaDtos.DetailResponse upload(
            MediaKind kind,
            UUID ownerUserId,
            List<UUID> albumIds,
            MultipartFile file
    ) throws IOException {
        if (!userRepository.existsById(ownerUserId)) {
            throw new NotFoundException("user not found");
        }

        validateAlbums(ownerUserId, albumIds);

        String bucket = bucketFor(kind);
        String key = storageService.upload(bucket, file);
        String contentType = file.getContentType() == null || file.getContentType().isBlank()
                ? "application/octet-stream"
                : file.getContentType();

        MediaFilesRecord record = repository.create(
                ownerUserId,
                kind.name(),
                bucket,
                key,
                originalFilename(file),
                contentType,
                file.getSize()
        );

        if (albumIds != null) {
            for (UUID albumId : albumIds) {
                repository.addToAlbum(albumId, record.getFileId());
            }
        }

        return toDetailResponse(record);
    }

    public List<MediaDtos.ListResponse> findAll(UUID ownerUserId) {
        if (ownerUserId != null && !userRepository.existsById(ownerUserId)) {
            throw new NotFoundException("user not found");
        }

        return repository.findAll(ownerUserId)
                .stream()
                .map(this::toListResponse)
                .toList();
    }

    public MediaDtos.DetailResponse findById(UUID fileId) {
        MediaFilesRecord record = repository.findById(fileId)
                .orElseThrow(() -> new NotFoundException("file not found"));

        return toDetailResponse(record);
    }

    public List<MediaDtos.ListResponse> findByAlbumId(UUID albumId) {
        if (!albumRepository.existsById(albumId)) {
            throw new NotFoundException("album not found");
        }

        return repository.findByAlbumId(albumId)
                .stream()
                .map(this::toListResponse)
                .toList();
    }

    public ResponseInputStream<GetObjectResponse> download(UUID fileId) {
        MediaFilesRecord record = repository.findById(fileId)
                .orElseThrow(() -> new NotFoundException("file not found"));

        return storageService.download(record.getBucket(), record.getObjectKey());
    }

    public void delete(UUID fileId) {
        MediaFilesRecord record = repository.findById(fileId)
                .orElseThrow(() -> new NotFoundException("file not found"));

        storageService.delete(record.getBucket(), record.getObjectKey());
        repository.deleteById(fileId);
    }

    public void addToAlbum(UUID albumId, UUID fileId) {
        MediaFilesRecord record = repository.findById(fileId)
                .orElseThrow(() -> new NotFoundException("file not found"));

        if (!albumRepository.existsByIdAndOwnerUserId(albumId, record.getOwnerUserId())) {
            throw new NotFoundException("album not found");
        }

        repository.addToAlbum(albumId, fileId);
    }

    public void removeFromAlbum(UUID albumId, UUID fileId) {
        if (!repository.findById(fileId).isPresent()) {
            throw new NotFoundException("file not found");
        }

        if (!albumRepository.existsById(albumId)) {
            throw new NotFoundException("album not found");
        }

        repository.removeFromAlbum(albumId, fileId);
    }

    private void validateAlbums(UUID ownerUserId, List<UUID> albumIds) {
        if (albumIds == null) {
            return;
        }

        for (UUID albumId : albumIds) {
            if (!albumRepository.existsByIdAndOwnerUserId(albumId, ownerUserId)) {
                throw new NotFoundException("album not found");
            }
        }
    }

    private String bucketFor(MediaKind kind) {
        return switch (kind) {
            case IMAGE -> IMAGE_BUCKET;
            case VIDEO -> VIDEO_BUCKET;
        };
    }

    private String originalFilename(MultipartFile file) {
        return file.getOriginalFilename() == null || file.getOriginalFilename().isBlank()
                ? "file"
                : file.getOriginalFilename();
    }

    private MediaDtos.ListResponse toListResponse(MediaFilesRecord record) {
        return new MediaDtos.ListResponse(
                record.getFileId(),
                record.getOwnerUserId(),
                MediaKind.valueOf(record.getKind()),
                record.getOriginalFilename(),
                record.getBucket(),
                record.getObjectKey(),
                record.getContentType(),
                record.getSizeBytes(),
                record.getUploadedAt()
        );
    }

    private MediaDtos.DetailResponse toDetailResponse(MediaFilesRecord record) {
        List<MediaDtos.AlbumReference> albums = repository.findAlbumsForFile(record.getFileId())
                .stream()
                .map(album -> new MediaDtos.AlbumReference(album.value1(), album.value2()))
                .toList();

        return new MediaDtos.DetailResponse(
                record.getFileId(),
                record.getOwnerUserId(),
                MediaKind.valueOf(record.getKind()),
                record.getOriginalFilename(),
                record.getBucket(),
                record.getObjectKey(),
                record.getContentType(),
                record.getSizeBytes(),
                record.getUploadedAt(),
                albums
        );
    }
}
