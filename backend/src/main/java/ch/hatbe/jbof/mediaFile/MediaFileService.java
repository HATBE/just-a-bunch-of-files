package ch.hatbe.jbof.mediaFile;

import ch.hatbe.jbof.album.AlbumRepository;
import ch.hatbe.jbof.album.entity.Album;
import ch.hatbe.jbof.core.security.AuthenticatedUserService;
import ch.hatbe.jbof.core.exception.NotFoundException;
import ch.hatbe.jbof.mediaFile.entity.*;
import ch.hatbe.jbof.mediaFile.entity.dto.MediaFileDetailDto;
import ch.hatbe.jbof.mediaFile.entity.dto.MediaFileListDto;
import ch.hatbe.jbof.mediaFile.entity.requests.CreateMediaFileRequest;
import ch.hatbe.jbof.messaging.MessagingService;
import ch.hatbe.jbof.storage.FileService;
import ch.hatbe.jbof.storage.StorageService;
import ch.hatbe.jbof.user.entity.User;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.InputStreamResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class MediaFileService {
    private final MediaFileRepository mediaFileRepository;
    private final AlbumRepository albumRepository;
    private final FileService fileService;
    private final StorageService storageService;
    private final AuthenticatedUserService authenticatedUserService;
    private final MessagingService messagingService;

    @Transactional(readOnly = true)
    public Page<MediaFileListDto> findAll(Pageable pageable) {
        User currentUser = this.authenticatedUserService.getCurrentUser();

        return mediaFileRepository.findAllVisibleForOwner(currentUser.getUserId(), MediaProcessingStatus.READY, pageable)
                .map(MediaFileMapper::toListDto);
    }

    @Transactional(readOnly = true)
    public Optional<MediaFileDetailDto> findById(UUID id) {
        User currentUser = this.authenticatedUserService.getCurrentUser();

        return mediaFileRepository.findByMediaFileIdAndOwnerUserIdAndProcessingStatus(id, currentUser.getUserId(), MediaProcessingStatus.READY)
                .map(MediaFileMapper::toDetailDto);
    }

    @Transactional(readOnly = true)
    public Optional<StoredMediaContent> findThumbnailContent(UUID id) {
        User currentUser = this.authenticatedUserService.getCurrentUser();

        return mediaFileRepository.findByMediaFileIdAndOwnerUserIdAndProcessingStatus(id, currentUser.getUserId(), MediaProcessingStatus.READY)
                .flatMap(this::resolveThumbnailContent);
    }

    @Transactional
    public List<UUID> create(CreateMediaFileRequest request) throws Exception {
        User owner = this.authenticatedUserService.getCurrentUser();

        List<Album> albums = this.resolveAlbums(owner, request.albumIds());

        return this.uploadFiles(request.files(), owner, albums);
    }

    private List<UUID> uploadFiles(List<MultipartFile> files, User owner, List<Album> albums) throws Exception {
        List<UUID> uuidsOfUploadedFiles = new ArrayList<>(files.size());
        List<StoredObject> storedObjects = new ArrayList<>(files.size());

        try {
            for (MultipartFile file : files) {
                UploadResult uploadResult = this.uploadSingleFile(file, owner, albums);
                uuidsOfUploadedFiles.add(uploadResult.mediaFileId());
                storedObjects.add(uploadResult.storedObject());
            }
        } catch (Exception e) {
            this.cleanupStoredObjects(storedObjects);
            throw e;
        }

        return uuidsOfUploadedFiles;
    }

    private UploadResult uploadSingleFile(MultipartFile file, User owner, List<Album> albums) throws Exception {
        String contentType = this.fileService.getEffectiveContentType(file);
        MediaKind kind = this.fileService.detectKind(file);
        String bucket = bucketFor(kind);
        String key = this.storageService.upload(bucket, file);
        StoredObject storedObject = new StoredObject(bucket, key);

        try {
            MediaFile mediaFile = new MediaFile();
            OffsetDateTime now = OffsetDateTime.now();
            MediaMetadata metadata = new MediaMetadata();

            mediaFile.setOwner(owner);
            mediaFile.setProcessingStatus(MediaProcessingStatus.UPLOADED);
            mediaFile.setContentType(contentType);
            mediaFile.setKind(kind);
            mediaFile.setCreatedAt(now);
            mediaFile.setOriginalFilename(file.getOriginalFilename());
            mediaFile.setBucket(bucket);
            mediaFile.setObjectKey(key);
            mediaFile.setMetadata(metadata);

            metadata.setMediaFile(mediaFile);
            metadata.setSizeBytes(file.getSize());
            metadata.setChecksumSha256(this.fileService.sha256(file));
            metadata.setCapturedAt(this.fileService.getCapturedAt(file, kind));
            metadata.setUploadedAt(now);
            metadata.setCreatedAt(now);
            metadata.setUpdatedAt(now);
            metadata.setMetadataJson("{}");

            MediaFile createdFile = mediaFileRepository.save(mediaFile);
            this.attachToAlbums(createdFile, albums);

            this.messagingService.sendToQueue(new MediaFileQueueMessage(createdFile.getMediaFileId(), OffsetDateTime.now()));

            return new UploadResult(createdFile.getMediaFileId(), storedObject);
        } catch (Exception e) {
            this.storageService.deleteQuietly(storedObject.bucket(), storedObject.key());
            throw e;
        }
    }

    private List<Album> resolveAlbums(@NotNull User owner, List<UUID> albumIds) {
        if (albumIds == null || albumIds.isEmpty()) {
            return Collections.emptyList();
        }

        List<Album> albums = new ArrayList<>(albumIds.size());
        for (UUID albumId : albumIds) {
            Album album = this.albumRepository.findByAlbumIdAndOwnerUserId(albumId, owner.getUserId())
                    .orElseThrow(() -> new NotFoundException(String.format("Album with id %s not found", albumId)));

            albums.add(album);
        }

        return albums;
    }

    private void attachToAlbums(MediaFile mediaFile, List<Album> albums) {
        for (Album album : albums) {
            album.getMediaFiles().add(mediaFile);
        }
    }

    private String bucketFor(MediaKind kind) {
        return switch (kind) {
            case IMAGE -> "images";
            case VIDEO -> "videos";
        };
    }

    private void cleanupStoredObjects(List<StoredObject> storedObjects) {
        for (StoredObject storedObject : storedObjects) {
            this.storageService.deleteQuietly(storedObject.bucket(), storedObject.key());
        }
    }

    private Optional<StoredMediaContent> resolveThumbnailContent(MediaFile mediaFile) {
        return mediaFile.getDerivatives().stream()
                .filter(derivative -> derivative.getKind() == MediaDerivativeKind.THUMBNAIL)
                .findFirst()
                .map(derivative -> new StoredMediaContent(
                        new InputStreamResource(this.storageService.download(derivative.getBucket(), derivative.getObjectKey())),
                        derivative.getContentType()
                ))
                .or(() -> this.originalImageContent(mediaFile));
    }

    private Optional<StoredMediaContent> originalImageContent(MediaFile mediaFile) {
        if (mediaFile.getKind() != MediaKind.IMAGE) {
            return Optional.empty();
        }

        return Optional.of(new StoredMediaContent(
                new InputStreamResource(this.storageService.download(mediaFile.getBucket(), mediaFile.getObjectKey())),
                mediaFile.getContentType()
        ));
    }

    private record StoredObject(String bucket, String key) {}

    private record UploadResult(UUID mediaFileId, StoredObject storedObject) {}

    public record StoredMediaContent(InputStreamResource resource, String contentType) {}
}
