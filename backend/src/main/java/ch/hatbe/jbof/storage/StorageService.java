package ch.hatbe.jbof.storage;

import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.CreateBucketRequest;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.HeadBucketRequest;
import software.amazon.awssdk.services.s3.model.NoSuchBucketException;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;

import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class StorageService {
    private final S3Client client;

    public String upload(String bucket, MultipartFile file) throws IOException {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("file is empty");
        }

        return upload(
                bucket,
                file.getOriginalFilename(),
                file.getContentType(),
                file.getInputStream(),
                file.getSize()
        );
    }

    public String upload(
            String bucket,
            String originalFilename,
            String contentType,
            InputStream inputStream,
            long size
    ) {
        validateBucket(bucket);

        if (inputStream == null) {
            throw new IllegalArgumentException("input stream is empty");
        }

        if (size < 0) {
            throw new IllegalArgumentException("size must be positive");
        }

        ensureBucketExists(bucket);

        String key = buildKey(originalFilename);
        String effectiveContentType = contentType;
        if (effectiveContentType == null || effectiveContentType.isBlank()) {
            effectiveContentType = MediaType.APPLICATION_OCTET_STREAM_VALUE;
        }

        PutObjectRequest request = PutObjectRequest.builder()
                .bucket(bucket)
                .key(key)
                .contentType(effectiveContentType)
                .build();

        client.putObject(request, RequestBody.fromInputStream(inputStream, size));

        return key;
    }

    public ResponseInputStream<GetObjectResponse> download(String bucket, String key) {
        validateBucket(bucket);

        if (key == null || key.isBlank()) {
            throw new IllegalArgumentException("key is empty");
        }

        GetObjectRequest request = GetObjectRequest.builder()
                .bucket(bucket)
                .key(key)
                .build();

        return client.getObject(request);
    }

    public void delete(String bucket, String key) {
        validateBucket(bucket);

        if (key == null || key.isBlank()) {
            throw new IllegalArgumentException("key is empty");
        }

        DeleteObjectRequest request = DeleteObjectRequest.builder()
                .bucket(bucket)
                .key(key)
                .build();

        client.deleteObject(request);
    }

    private void ensureBucketExists(String bucket) {
        try {
            client.headBucket(HeadBucketRequest.builder().bucket(bucket).build());
        } catch (NoSuchBucketException e) {
            client.createBucket(CreateBucketRequest.builder().bucket(bucket).build());
        } catch (S3Exception e) {
            if (e.statusCode() == 404) {
                client.createBucket(CreateBucketRequest.builder().bucket(bucket).build());
                return;
            }
            throw e;
        }
    }

    private void validateBucket(String bucket) {
        if (bucket == null || bucket.isBlank()) {
            throw new IllegalArgumentException("bucket is empty");
        }
    }

    private String buildKey(String originalFilename) {
        String extension = extractExtension(originalFilename);
        return extension.isEmpty()
                ? UUID.randomUUID().toString()
                : UUID.randomUUID() + "." + extension;
    }

    private String extractExtension(String originalFilename) {
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
}
