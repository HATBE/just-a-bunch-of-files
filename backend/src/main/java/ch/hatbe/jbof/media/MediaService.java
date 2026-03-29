package ch.hatbe.jbof.media;

import ch.hatbe.jbof.album.AlbumRepository;
import ch.hatbe.jbof.core.exception.NotFoundException;
import ch.hatbe.jbof.core.pagination.PageQuery;
import ch.hatbe.jbof.core.pagination.PageRequest;
import ch.hatbe.jbof.core.pagination.PageResult;
import ch.hatbe.jbof.jooq.tables.records.MediaFilesRecord;
import ch.hatbe.jbof.messaging.RabbitMqProperties;
import ch.hatbe.jbof.messaging.RabbitMqService;
import ch.hatbe.jbof.media.entity.MediaDtos;
import ch.hatbe.jbof.media.entity.MediaKind;
import ch.hatbe.jbof.storage.StorageService;
import ch.hatbe.jbof.user.UserRepository;
import ch.hatbe.jbof.user.entity.UserDtos;
import ch.hatbe.jbof.user.UserMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class MediaService {
    private static final String STAGING_BUCKET = "media-staging";

    private final MediaRepository repository;
    private final UserRepository userRepository;
    private final AlbumRepository albumRepository;
    private final StorageService storageService;
    private final RabbitMqService rabbitMqService;
    private final RabbitMqProperties rabbitMqProperties;
    private final MediaMapper mediaMapper;
    private final UserMapper userMapper;

    public List<MediaDtos.DetailResponse> upload(
            UUID userId,
            List<UUID> albumIds,
            List<MultipartFile> files
    ) throws IOException {
        if (!userRepository.existsById(userId)) {
            throw new NotFoundException("user not found");
        }

        validateAlbums(userId, albumIds);

        if (files == null || files.isEmpty()) {
            throw new IllegalArgumentException("There are no files");
        }

        List<MediaDtos.DetailResponse> uploadedFiles = new java.util.ArrayList<>(files.size());
        for (MultipartFile file : files) {
            uploadedFiles.add(uploadSingle(userId, albumIds, file));
        }

        return uploadedFiles;
    }

    public PageResult<MediaDtos.ListResponse> findAll(UUID userId, PageQuery pageQuery) {
        if (userId != null && !userRepository.existsById(userId)) {
            throw new NotFoundException("user not found");
        }

        PageRequest pageRequest = pageQuery.toPageRequest();

        List<MediaDtos.ListResponse> slice = repository.findAll(userId, pageRequest)
                .stream()
                .map(record -> mediaMapper.toListResponse(record, getUserSummary(record.getOwnerUserId())))
                .toList();

        return PageResult.fromSlice(slice, pageRequest);
    }

    public List<MediaDtos.ListResponse> findByAlbumId(UUID albumId) {
        if (!albumRepository.existsById(albumId)) {
            throw new NotFoundException("album not found");
        }

        return repository.findByAlbumId(albumId)
                .stream()
                .map(record -> mediaMapper.toListResponse(record, getUserSummary(record.getOwnerUserId())))
                .toList();
    }

    public MediaDtos.DetailResponse findById(UUID fileId) {
        MediaFilesRecord record = repository.findById(fileId)
                .orElseThrow(() -> new NotFoundException("file not found"));

        return toDetailResponse(record);
    }

    public ResponseInputStream<GetObjectResponse> download(UUID fileId) {
        MediaFilesRecord record = repository.findById(fileId)
                .orElseThrow(() -> new NotFoundException("file not found"));

        return storageService.download(record.getBucket(), record.getObjectKey());
    }

    public MediaDownload preview(UUID fileId) {
        MediaFilesRecord record = repository.findById(fileId)
                .orElseThrow(() -> new NotFoundException("file not found"));

        String bucket = record.getThumbnailBucket() == null || record.getThumbnailBucket().isBlank()
                ? record.getBucket()
                : record.getThumbnailBucket();
        String objectKey = record.getThumbnailObjectKey() == null || record.getThumbnailObjectKey().isBlank()
                ? record.getObjectKey()
                : record.getThumbnailObjectKey();
        String contentType = record.getThumbnailContentType() == null || record.getThumbnailContentType().isBlank()
                ? record.getContentType()
                : record.getThumbnailContentType();
        long sizeBytes = record.getThumbnailSizeBytes() == null
                ? record.getSizeBytes()
                : record.getThumbnailSizeBytes();

        return new MediaDownload(
                originalFilename(record.getOriginalFilename()),
                contentType,
                sizeBytes,
                storageService.download(bucket, objectKey)
        );
    }

    public void delete(UUID fileId) {
        MediaFilesRecord record = repository.findExistingById(fileId)
                .orElseThrow(() -> new NotFoundException("file not found"));

        storageService.delete(record.getBucket(), record.getObjectKey());
        if (record.getThumbnailBucket() != null && record.getThumbnailObjectKey() != null) {
            storageService.delete(record.getThumbnailBucket(), record.getThumbnailObjectKey());
        }
        repository.deleteById(fileId);
    }

    public void addToAlbum(UUID albumId, UUID fileId) {
        MediaFilesRecord record = repository.findExistingById(fileId)
                .orElseThrow(() -> new NotFoundException("file not found"));

        if (!albumRepository.existsByIdAndOwnerUserId(albumId, record.getOwnerUserId())) {
            throw new NotFoundException("album not found");
        }

        repository.addToAlbum(albumId, fileId);
    }

    public void removeFromAlbum(UUID albumId, UUID fileId) {
        if (repository.findExistingById(fileId).isEmpty()) {
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

    private MediaKind detectKind(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("file is empty");
        }

        String contentType = file.getContentType();
        if (contentType == null || contentType.isBlank()) {
            throw new IllegalArgumentException("content type is missing");
        }

        if (contentType.startsWith("image/")) {
            return MediaKind.IMAGE;
        }

        if (contentType.startsWith("video/")) {
            return MediaKind.VIDEO;
        }

        throw new IllegalArgumentException("only image and video uploads are supported");
    }

    private String originalFilename(MultipartFile file) {
        return file.getOriginalFilename() == null || file.getOriginalFilename().isBlank()
                ? "file"
                : file.getOriginalFilename();
    }

    private String originalFilename(String originalFilename) {
        return originalFilename == null || originalFilename.isBlank()
                ? "file"
                : originalFilename;
    }

    private MediaDtos.DetailResponse uploadSingle(
            UUID userId,
            List<UUID> albumIds,
            MultipartFile file
    ) throws IOException {
        MediaKind kind = detectKind(file);

        String bucket = STAGING_BUCKET;
        String key = null;
        UUID fileId = null;

        try {
            key = storageService.upload(bucket, file);
            String contentType = file.getContentType() == null || file.getContentType().isBlank()
                    ? MediaType.APPLICATION_OCTET_STREAM_VALUE
                    : file.getContentType();

            MediaFilesRecord record = repository.create(
                    userId,
                    kind.name(),
                    bucket,
                    key,
                    null,
                    null,
                    originalFilename(file),
                    contentType,
                    file.getSize(),
                    null,
                    null,
                    null
            );
            fileId = record.getFileId();

            if (albumIds != null) {
                for (UUID albumId : albumIds) {
                    repository.addToAlbum(albumId, record.getFileId());
                }
            }

            rabbitMqService.sendToQueue(
                    rabbitMqProperties.mediaProcessingQueue(),
                    new MediaProcessingTask(record.getFileId())
            );

            return toDetailResponse(record);
        } catch (Exception e) {
            if (fileId != null) {
                repository.deleteById(fileId);
            }
            cleanupUpload(bucket, key);

            if (e instanceof IOException ioException) {
                throw ioException;
            }

            throw e;
        }
    }

    private MediaDtos.DetailResponse toDetailResponse(MediaFilesRecord record) {
        List<MediaDtos.AlbumReference> albums = repository.findAlbumsForFile(record.getFileId())
                .stream()
                .map(album -> new MediaDtos.AlbumReference(album.value1(), album.value2()))
                .toList();

        return mediaMapper.toDetailResponse(record, getUserSummary(record.getOwnerUserId()), albums);
    }

    private UserDtos.ListResponse getUserSummary(UUID userId) {
        return userRepository.findById(userId)
                .map(userMapper::toListResponse)
                .orElseThrow(() -> new NotFoundException("user not found"));
    }

    private void cleanupUpload(String bucket, String key) {
        if (bucket == null || key == null) {
            return;
        }

        try {
            storageService.delete(bucket, key);
        } catch (Exception cleanupError) {
            log.warn("Could not clean up uploaded object {}/{}", bucket, key, cleanupError);
        }
    }

    public record MediaDownload(
            String originalFilename,
            String contentType,
            long sizeBytes,
            ResponseInputStream<GetObjectResponse> stream
    ) { }
}
