package ch.hatbe.jbof.media;

import ch.hatbe.jbof.album.AlbumRepository;
import ch.hatbe.jbof.core.exception.NotFoundException;
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
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;

import java.io.IOException;
import java.io.InputStream;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class MediaService {
    private static final String IMAGE_BUCKET = "images";
    private static final String VIDEO_BUCKET = "videos";

    private final MediaRepository repository;
    private final UserRepository userRepository;
    private final AlbumRepository albumRepository;
    private final StorageService storageService;
    private final MediaMapper mediaMapper;
    private final UserMapper userMapper;

    public MediaDtos.DetailResponse upload(
            UUID userId,
            List<UUID> albumIds,
            MultipartFile file
    ) throws IOException {
        if (!userRepository.existsById(userId)) {
            throw new NotFoundException("user not found");
        }

        validateAlbums(userId, albumIds);

        MediaKind kind = detectKind(file);

        String bucket = bucketFor(kind);
        String key = storageService.upload(bucket, file);
        String contentType = file.getContentType() == null || file.getContentType().isBlank()
                ? MediaType.APPLICATION_OCTET_STREAM_VALUE
                : file.getContentType();

        MediaFilesRecord record = repository.create(
                userId,
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

        logCapturedAt(file, kind);

        return toDetailResponse(record);
    }

    public List<MediaDtos.ListResponse> findAll(UUID userId) {
        if (userId != null && !userRepository.existsById(userId)) {
            throw new NotFoundException("user not found");
        }

        return repository.findAll(userId)
                .stream()
                .map(record -> mediaMapper.toListResponse(record, getUserSummary(record.getOwnerUserId())))
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
                .map(record -> mediaMapper.toListResponse(record, getUserSummary(record.getOwnerUserId())))
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


    private void logCapturedAt(MultipartFile file, MediaKind kind) {
        if (kind != MediaKind.IMAGE) {
            log.info("Capture timestamp extraction is not implemented yet for {} {}", kind, originalFilename(file));
            return;
        }

        try (InputStream inputStream = file.getInputStream()) {
            Metadata metadata = ImageMetadataReader.readMetadata(inputStream);
            Date capturedAt = readCapturedAt(metadata);

            if (capturedAt != null) {
                log.info("Captured at for {}: {}", originalFilename(file), capturedAt);
                return;
            }

            log.info("No embedded capture timestamp found for {}", originalFilename(file));
        } catch (ImageProcessingException | IOException e) {
            log.warn("Could not extract captured-at metadata for {}", originalFilename(file), e);
        }
    }

    private Date readCapturedAt(Metadata metadata) {
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
            return exifIfd0.getDate(ExifIFD0Directory.TAG_DATETIME);
        }

        return null;
    }
}
