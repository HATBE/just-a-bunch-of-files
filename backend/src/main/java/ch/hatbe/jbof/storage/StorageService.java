package ch.hatbe.jbof.storage;

import lombok.RequiredArgsConstructor;
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
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class StorageService {
    private final S3Client client;
    private final FileService fileService;

    public String upload(
            String bucket,
            MultipartFile file
    ) throws IOException {
        this.ensureBucketExists(bucket);

        String key = this.buildKey(file);

        PutObjectRequest request = PutObjectRequest.builder()
                .bucket(bucket)
                .key(key)
                .contentType(this.fileService.getEffectiveContentType(file))
                .build();

        client.putObject(request, RequestBody.fromInputStream(file.getInputStream(), file.getSize()));

        return key;
    }

    public ResponseInputStream<GetObjectResponse> download(String bucket, String key) {
        GetObjectRequest request = GetObjectRequest.builder()
                .bucket(bucket)
                .key(key)
                .build();

        return client.getObject(request);
    }

    public void delete(String bucket, String key) {
        DeleteObjectRequest request = DeleteObjectRequest.builder()
                .bucket(bucket)
                .key(key)
                .build();

        client.deleteObject(request);
    }

    public void deleteQuietly(String bucket, String key) {
        try {
            delete(bucket, key);
        } catch (RuntimeException ignored) {
        }
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

    private String buildKey(MultipartFile file) {
        String extension = this.fileService.extractExtension(file);
        return extension.isEmpty()
                ? UUID.randomUUID().toString()
                : UUID.randomUUID() + "." + extension;
    }
}
