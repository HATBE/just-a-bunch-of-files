package ch.hatbe.jbof.media;

import ch.hatbe.jbof.media.entity.MediaKind;
import com.drew.imaging.ImageMetadataReader;
import com.drew.imaging.ImageProcessingException;
import com.drew.metadata.Metadata;
import com.drew.metadata.exif.ExifDirectoryBase;
import com.drew.metadata.exif.ExifIFD0Directory;
import lombok.extern.slf4j.Slf4j;
import net.coobird.thumbnailator.Thumbnails;
import org.jcodec.api.JCodecException;
import org.jcodec.api.awt.AWTFrameGrab;
import org.jcodec.common.io.NIOUtils;
import org.jcodec.common.io.SeekableByteChannel;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.StandardCopyOption;
import java.nio.file.Files;
import java.nio.file.Path;

@Service
@Slf4j
public class ThumbnailService {
    private static final int MAX_THUMBNAIL_DIMENSION = 480;
    private static final String THUMBNAIL_CONTENT_TYPE = "image/jpeg";
    private static final String THUMBNAIL_FILENAME = "thumbnail.jpg";

    public ThumbnailPayload createThumbnail(MediaKind kind, MultipartFile file) throws IOException {
        return switch (kind) {
            case IMAGE -> createImageThumbnail(file);
            case VIDEO -> createVideoThumbnail(file);
        };
    }

    private ThumbnailPayload createImageThumbnail(MultipartFile file) throws IOException {
        byte[] bytes = file.getBytes();

        try (InputStream metadataStream = new java.io.ByteArrayInputStream(bytes);
             InputStream imageStream = new java.io.ByteArrayInputStream(bytes)) {
            Metadata metadata = ImageMetadataReader.readMetadata(metadataStream);
            BufferedImage source = ImageIO.read(imageStream);
            if (source == null) {
                throw new IOException("unsupported image format");
            }

            return encodeThumbnail(applyImageOrientation(source, readImageOrientation(metadata)));
        } catch (ImageProcessingException e) {
            throw new IOException("image metadata extraction failed", e);
        }
    }

    private ThumbnailPayload createVideoThumbnail(MultipartFile file) throws IOException {
        Path tempFile = Files.createTempFile("jbof-video-thumbnail-", ".bin");

        try {
            try (InputStream inputStream = file.getInputStream()) {
                Files.copy(inputStream, tempFile, StandardCopyOption.REPLACE_EXISTING);
            }

            BufferedImage frame = extractFrame(tempFile);
            return encodeThumbnail(frame);
        } finally {
            Files.deleteIfExists(tempFile);
        }
    }

    private BufferedImage extractFrame(Path videoFile) throws IOException {
        try (SeekableByteChannel channel = NIOUtils.readableChannel(videoFile.toFile())) {
            AWTFrameGrab frameGrab = AWTFrameGrab.createAWTFrameGrab(channel);
            try {
                frameGrab.seekToSecondPrecise(1);
                return frameGrab.getFrameWithOrientation();
            } catch (JCodecException ignored) {
                try (SeekableByteChannel fallbackChannel = NIOUtils.readableChannel(videoFile.toFile())) {
                    AWTFrameGrab fallbackGrab = AWTFrameGrab.createAWTFrameGrab(fallbackChannel);
                    fallbackGrab.seekToFramePrecise(0);
                    return fallbackGrab.getFrameWithOrientation();
                }
            }
        } catch (JCodecException e) {
            throw new IOException("video frame extraction failed", e);
        }
    }

    private int readImageOrientation(Metadata metadata) {
        ExifIFD0Directory exifIfd0 = metadata.getFirstDirectoryOfType(ExifIFD0Directory.class);
        if (exifIfd0 == null) {
            return 1;
        }

        Integer orientation = exifIfd0.getInteger(ExifDirectoryBase.TAG_ORIENTATION);
        return orientation == null ? 1 : orientation;
    }

    private BufferedImage applyImageOrientation(BufferedImage source, int orientation) {
        int width = source.getWidth();
        int height = source.getHeight();
        int targetWidth = switch (orientation) {
            case 5, 6, 7, 8 -> height;
            default -> width;
        };
        int targetHeight = switch (orientation) {
            case 5, 6, 7, 8 -> width;
            default -> height;
        };

        if (orientation == 1) {
            return source;
        }

        BufferedImage rotated = new BufferedImage(targetWidth, targetHeight, BufferedImage.TYPE_INT_ARGB);
        Graphics2D graphics = rotated.createGraphics();
        try {
            graphics.setTransform(orientationTransform(orientation, width, height));
            graphics.drawImage(source, 0, 0, null);
        } finally {
            graphics.dispose();
        }

        return rotated;
    }

    private AffineTransform orientationTransform(int orientation, int width, int height) {
        return switch (orientation) {
            case 2 -> new AffineTransform(-1, 0, 0, 1, width, 0);
            case 3 -> new AffineTransform(-1, 0, 0, -1, width, height);
            case 4 -> new AffineTransform(1, 0, 0, -1, 0, height);
            case 5 -> new AffineTransform(0, 1, 1, 0, 0, 0);
            case 6 -> new AffineTransform(0, 1, -1, 0, height, 0);
            case 7 -> new AffineTransform(0, -1, -1, 0, height, width);
            case 8 -> new AffineTransform(0, -1, 1, 0, 0, width);
            default -> new AffineTransform();
        };
    }

    private ThumbnailPayload encodeThumbnail(BufferedImage source) throws IOException {
        BufferedImage rgbImage = toRgbImage(source);
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        Thumbnails.of(rgbImage)
                .size(MAX_THUMBNAIL_DIMENSION, MAX_THUMBNAIL_DIMENSION)
                .outputFormat("jpg")
                .outputQuality(0.85)
                .toOutputStream(outputStream);

        byte[] bytes = outputStream.toByteArray();
        return new ThumbnailPayload(THUMBNAIL_FILENAME, THUMBNAIL_CONTENT_TYPE, bytes);
    }

    private BufferedImage toRgbImage(BufferedImage source) {
        if (source.getType() == BufferedImage.TYPE_INT_RGB) {
            return source;
        }

        BufferedImage rgbImage = new BufferedImage(source.getWidth(), source.getHeight(), BufferedImage.TYPE_INT_RGB);
        Graphics2D graphics = rgbImage.createGraphics();
        try {
            graphics.setColor(Color.WHITE);
            graphics.fillRect(0, 0, rgbImage.getWidth(), rgbImage.getHeight());
            graphics.drawImage(source, 0, 0, null);
        } finally {
            graphics.dispose();
        }

        return rgbImage;
    }

    public record ThumbnailPayload(
            String filename,
            String contentType,
            byte[] bytes
    ) {
        public long sizeBytes() {
            return bytes.length;
        }
    }
}
