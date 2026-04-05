package ch.hatbe.jbof.storage;

import com.drew.imaging.avi.AviMetadataReader;
import ch.hatbe.jbof.mediaFile.entity.MediaKind;
import com.drew.imaging.ImageMetadataReader;
import com.drew.imaging.ImageProcessingException;
import com.drew.imaging.mp4.Mp4MetadataReader;
import com.drew.imaging.quicktime.QuickTimeMetadataReader;
import com.drew.metadata.Directory;
import com.drew.metadata.Metadata;
import com.drew.metadata.avi.AviDirectory;
import com.drew.metadata.exif.ExifIFD0Directory;
import com.drew.metadata.exif.ExifSubIFDDirectory;
import com.drew.metadata.mov.QuickTimeDirectory;
import com.drew.metadata.mp4.Mp4Directory;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.io.InputStream;
import java.security.MessageDigest;

import java.io.IOException;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Date;
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class FileService {
    public String getEffectiveContentType(MultipartFile file) {
        String effectiveContentType = file.getContentType();
        if (effectiveContentType == null || effectiveContentType.isBlank()) {
            effectiveContentType = MediaType.APPLICATION_OCTET_STREAM_VALUE;
        }
        return effectiveContentType;
    }

    public String extractExtension(MultipartFile file) {
        String originalFilename = file.getOriginalFilename();

        if (originalFilename == null || originalFilename.isBlank()) {
            return "";
        }

        String name = originalFilename.replace("\\", "/");
        int slashIndex = name.lastIndexOf('/');
        if (slashIndex >= 0) {
            name = name.substring(slashIndex + 1);
        }

        int dotIndex = name.indexOf('.');
        if (dotIndex <= 0 || dotIndex == name.length() - 1) {
            return "";
        }

        return name.substring(dotIndex + 1);
    }

    public MediaKind detectKind(MultipartFile file) {
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

    public OffsetDateTime getCapturedAt(MultipartFile file, MediaKind kind) {
        return switch (kind) {
            case IMAGE -> extractImageCapturedAt(file);
            case VIDEO -> extractVideoCapturedAt(file);
        };
    }

    private OffsetDateTime extractImageCapturedAt(MultipartFile file) {
        try (InputStream inputStream = file.getInputStream()) {
            return toOffsetDateTime(readImageCapturedAt(ImageMetadataReader.readMetadata(inputStream)));
        } catch (ImageProcessingException | IOException e) {
            return null;
        }
    }

    private OffsetDateTime extractVideoCapturedAt(MultipartFile file) {
        Date capturedAt = null;
        String contentType = getEffectiveContentType(file).toLowerCase(Locale.ROOT);
        String extension = extractExtension(file).toLowerCase(Locale.ROOT);

        if (isQuickTime(contentType, extension)) {
            capturedAt = readQuickTimeCapturedAt(file);
        }

        if (capturedAt == null && isMp4(contentType, extension)) {
            capturedAt = readMp4CapturedAt(file);
        }

        if (capturedAt == null && isAvi(contentType, extension)) {
            capturedAt = readAviCapturedAt(file);
        }

        if (capturedAt == null) {
            capturedAt = readMp4CapturedAt(file);
        }

        if (capturedAt == null) {
            capturedAt = readQuickTimeCapturedAt(file);
        }

        if (capturedAt == null) {
            capturedAt = readAviCapturedAt(file);
        }

        return toOffsetDateTime(capturedAt);
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
            return exifIfd0.getDate(ExifIFD0Directory.TAG_DATETIME);
        }

        return null;
    }

    private Date readQuickTimeCapturedAt(MultipartFile file) {
        Metadata metadata = readMetadata(file, QuickTimeMetadataReader::readMetadata);
        return readDate(metadata, QuickTimeDirectory.class, QuickTimeDirectory.TAG_CREATION_TIME);
    }

    private Date readMp4CapturedAt(MultipartFile file) {
        Metadata metadata = readMetadata(file, Mp4MetadataReader::readMetadata);
        return readDate(metadata, Mp4Directory.class, Mp4Directory.TAG_CREATION_TIME);
    }

    private Date readAviCapturedAt(MultipartFile file) {
        Metadata metadata = readMetadata(file, AviMetadataReader::readMetadata);
        return readDate(metadata, AviDirectory.class, AviDirectory.TAG_DATETIME_ORIGINAL);
    }

    private <T extends Directory> Date readDate(Metadata metadata, Class<T> directoryType, int tagType) {
        if (metadata == null) {
            return null;
        }

        T directory = metadata.getFirstDirectoryOfType(directoryType);
        if (directory == null) {
            return null;
        }

        return directory.getDate(tagType);
    }

    private Metadata readMetadata(MultipartFile file, MetadataReader reader) {
        try (InputStream inputStream = file.getInputStream()) {
            return reader.read(inputStream);
        } catch (Exception e) {
            return null;
        }
    }

    private OffsetDateTime toOffsetDateTime(Date capturedAt) {
        return capturedAt == null ? null : capturedAt.toInstant().atOffset(ZoneOffset.UTC);
    }

    private boolean isQuickTime(String contentType, String extension) {
        return contentType.contains("quicktime") || "mov".equals(extension) || "qt".equals(extension);
    }

    private boolean isMp4(String contentType, String extension) {
        return contentType.contains("mp4")
                || "mp4".equals(extension)
                || "m4v".equals(extension)
                || "m4p".equals(extension);
    }

    private boolean isAvi(String contentType, String extension) {
        return contentType.contains("avi") || "avi".equals(extension);
    }

    @FunctionalInterface
    private interface MetadataReader {
        Metadata read(InputStream inputStream) throws Exception;
    }

    public String sha256(MultipartFile file) throws Exception {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");

        try (InputStream is = file.getInputStream()) {
            byte[] buffer = new byte[8192];
            int read;

            while ((read = is.read(buffer)) != -1) {
                digest.update(buffer, 0, read);
            }
        }

        byte[] hash = digest.digest();
        return toHex(hash);
    }

    private String toHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder(bytes.length * 2);
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }
}
