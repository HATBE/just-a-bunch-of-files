package ch.hatbe.jbof.media;

import ch.hatbe.jbof.album.AlbumRepository;
import ch.hatbe.jbof.core.exception.NotFoundException;
import ch.hatbe.jbof.core.pagination.PageQuery;
import ch.hatbe.jbof.core.pagination.PageRequest;
import ch.hatbe.jbof.core.pagination.PageResult;
import ch.hatbe.jbof.jooq.tables.records.MediaFilesRecord;
import ch.hatbe.jbof.media.entity.MediaDtos;
import ch.hatbe.jbof.media.entity.MediaKind;
import ch.hatbe.jbof.storage.StorageService;
import ch.hatbe.jbof.user.UserRepository;
import ch.hatbe.jbof.user.entity.UserDtos;
import ch.hatbe.jbof.user.UserMapper;
import com.drew.imaging.ImageMetadataReader;
import com.drew.imaging.ImageProcessingException;
import com.drew.metadata.Metadata;
import com.drew.metadata.exif.ExifIFD0Directory;
import com.drew.metadata.exif.ExifSubIFDDirectory;
import com.drew.metadata.mov.QuickTimeDirectory;
import com.drew.metadata.mov.metadata.QuickTimeMetadataDirectory;
import com.drew.metadata.mp4.Mp4Directory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class MediaService {
    private static final int DEFAULT_PAGE_SIZE = 24;
    private static final int MAX_PAGE_SIZE = 100;
    private static final String IMAGE_BUCKET = "images";
    private static final String VIDEO_BUCKET = "videos";

    private final MediaRepository repository;
    private final UserRepository userRepository;
    private final AlbumRepository albumRepository;
    private final StorageService storageService;
    private final ThumbnailService thumbnailService;
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
            throw new IllegalArgumentException("files are empty");
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

        PageRequest pageRequest = pageQuery.toPageRequest(DEFAULT_PAGE_SIZE, MAX_PAGE_SIZE);

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

    public MediaDtos.DetailResponse regeneratePreview(UUID fileId) throws IOException {
        MediaFilesRecord record = repository.findById(fileId)
                .orElseThrow(() -> new NotFoundException("file not found"));

        MediaKind kind = MediaKind.valueOf(record.getKind());
        byte[] originalBytes;
        try (ResponseInputStream<GetObjectResponse> objectStream =
                     storageService.download(record.getBucket(), record.getObjectKey())) {
            originalBytes = objectStream.readAllBytes();
        }

        ThumbnailService.ThumbnailPayload thumbnail = thumbnailService.createThumbnail(kind, originalBytes);
        String thumbnailBucket = record.getThumbnailBucket() == null || record.getThumbnailBucket().isBlank()
                ? record.getBucket()
                : record.getThumbnailBucket();
        String previousThumbnailKey = record.getThumbnailObjectKey();
        String thumbnailKey = storageService.upload(
                thumbnailBucket,
                thumbnail.filename(),
                thumbnail.contentType(),
                new ByteArrayInputStream(thumbnail.bytes()),
                thumbnail.sizeBytes()
        );

        MediaFilesRecord updatedRecord = repository.updateThumbnail(
                fileId,
                thumbnailBucket,
                thumbnailKey,
                thumbnail.contentType(),
                thumbnail.sizeBytes()
        );

        if (previousThumbnailKey != null && !previousThumbnailKey.isBlank() && !previousThumbnailKey.equals(thumbnailKey)) {
            storageService.delete(thumbnailBucket, previousThumbnailKey);
        }

        return toDetailResponse(updatedRecord);
    }

    public void delete(UUID fileId) {
        MediaFilesRecord record = repository.findById(fileId)
                .orElseThrow(() -> new NotFoundException("file not found"));

        storageService.delete(record.getBucket(), record.getObjectKey());
        if (record.getThumbnailBucket() != null && record.getThumbnailObjectKey() != null) {
            storageService.delete(record.getThumbnailBucket(), record.getThumbnailObjectKey());
        }
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

        String bucket = bucketFor(kind);
        String key = null;
        String thumbnailKey = null;

        try {
            key = storageService.upload(bucket, file);
            ThumbnailService.ThumbnailPayload thumbnail = thumbnailService.createThumbnail(kind, file);
            thumbnailKey = storageService.upload(
                    bucket,
                    thumbnail.filename(),
                    thumbnail.contentType(),
                    new ByteArrayInputStream(thumbnail.bytes()),
                    thumbnail.sizeBytes()
            );
            OffsetDateTime capturedAt = extractCapturedAt(file, kind).orElse(null);
            String contentType = file.getContentType() == null || file.getContentType().isBlank()
                    ? MediaType.APPLICATION_OCTET_STREAM_VALUE
                    : file.getContentType();

            MediaFilesRecord record = repository.create(
                    userId,
                    kind.name(),
                    bucket,
                    key,
                    bucket,
                    thumbnailKey,
                    originalFilename(file),
                    contentType,
                    file.getSize(),
                    capturedAt,
                    thumbnail.contentType(),
                    thumbnail.sizeBytes()
            );

            if (albumIds != null) {
                for (UUID albumId : albumIds) {
                    repository.addToAlbum(albumId, record.getFileId());
                }
            }

            return toDetailResponse(record);
        } catch (Exception e) {
            cleanupUpload(bucket, key);
            cleanupUpload(bucket, thumbnailKey);

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


    private Optional<OffsetDateTime> extractCapturedAt(MultipartFile file, MediaKind kind) {
        try (InputStream inputStream = file.getInputStream()) {
            Metadata metadata = ImageMetadataReader.readMetadata(inputStream);
            Date capturedAt = readCapturedAt(metadata, kind);

            if (capturedAt != null) {
                OffsetDateTime capturedAtValue = OffsetDateTime.ofInstant(capturedAt.toInstant(), ZoneOffset.UTC);
                log.info("Captured at for {} {}: {}", kind, originalFilename(file), capturedAtValue);
                return Optional.of(capturedAtValue);
            }

            log.info("No embedded capture timestamp found for {} {}", kind, originalFilename(file));
        } catch (ImageProcessingException | IOException e) {
            log.warn("Could not extract captured-at metadata for {}", originalFilename(file), e);
        }

        return Optional.empty();
    }

    private Date readCapturedAt(Metadata metadata, MediaKind kind) {
        return switch (kind) {
            case IMAGE -> readImageCapturedAt(metadata);
            case VIDEO -> readVideoCapturedAt(metadata);
        };
    }

    private Date readImageCapturedAt(Metadata metadata) {
        ExifSubIFDDirectory exifSubIfd = metadata.getFirstDirectoryOfType(ExifSubIFDDirectory.class);
        if (exifSubIfd != null) {
            Date original = exifSubIfd.getDateOriginal();
            if (original != null) {
                return original;
            }

            Date digitized = exifSubIfd.getDateDigitized();
            if (digitized != null) {
                return digitized;
            }
        }

        ExifIFD0Directory exifIfd0 = metadata.getFirstDirectoryOfType(ExifIFD0Directory.class);
        if (exifIfd0 != null) {
            Date date = exifIfd0.getDate(ExifIFD0Directory.TAG_DATETIME);
            if (date != null) {
                return date;
            }
        }

        return null;
    }

    private Date readVideoCapturedAt(Metadata metadata) {
        QuickTimeMetadataDirectory quickTimeMetadata = metadata.getFirstDirectoryOfType(QuickTimeMetadataDirectory.class);
        if (quickTimeMetadata != null) {
            Date date = quickTimeMetadata.getDate(QuickTimeMetadataDirectory.TAG_CREATION_DATE);
            if (date != null) {
                return date;
            }
        }

        QuickTimeDirectory quickTimeDirectory = metadata.getFirstDirectoryOfType(QuickTimeDirectory.class);
        if (quickTimeDirectory != null) {
            Date date = quickTimeDirectory.getDate(QuickTimeDirectory.TAG_CREATION_TIME);
            if (date != null) {
                return date;
            }
        }

        Mp4Directory mp4Directory = metadata.getFirstDirectoryOfType(Mp4Directory.class);
        if (mp4Directory != null) {
            Date date = mp4Directory.getDate(Mp4Directory.TAG_CREATION_TIME);
            if (date != null) {
                return date;
            }
        }

        return null;
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
