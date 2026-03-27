package ch.hatbe.jbof.media;

import ch.hatbe.jbof.media.entity.MediaKind;
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
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
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
        try (InputStream inputStream = file.getInputStream()) {
            BufferedImage source = ImageIO.read(inputStream);
            if (source == null) {
                throw new IOException("unsupported image format");
            }

            return encodeThumbnail(source);
        }
    }

    private ThumbnailPayload createVideoThumbnail(MultipartFile file) throws IOException {
        Path tempFile = Files.createTempFile("jbof-video-thumbnail-", ".bin");

        try {
            try (InputStream inputStream = file.getInputStream()) {
                Files.copy(inputStream, tempFile, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
            }

            BufferedImage frame = extractFrame(tempFile);
            return encodeThumbnail(frame);
        } finally {
            Files.deleteIfExists(tempFile);
        }
    }

    private BufferedImage extractFrame(Path videoFile) throws IOException {
        try (SeekableByteChannel channel = NIOUtils.readableChannel(videoFile.toFile())) {
            try {
                return AWTFrameGrab.getFrame(channel, 1.0);
            } catch (JCodecException ignored) {
                return AWTFrameGrab.getFrame(videoFile.toFile(), 0);
            }
        } catch (JCodecException e) {
            throw new IOException("video frame extraction failed", e);
        }
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
