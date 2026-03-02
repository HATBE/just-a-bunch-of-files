package ch.hatbe.jbof.storage;

import ch.hatbe.jbof.core.config.S3Config;
import ch.hatbe.jbof.storage.entity.ObjectInfo;
import ch.hatbe.jbof.storage.entity.StorageObjectEntity;
import ch.hatbe.jbof.storage.entity.UploadResult;
import lombok.RequiredArgsConstructor;
import org.apache.tika.Tika;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;

import java.io.InputStream;
import java.time.Instant;
import java.util.*;

@Service
@RequiredArgsConstructor
public class StorageService {

    private final S3Client s3;
    private final S3Config.S3Props props;
    private final StorageObjectRepository repo;

    private final Tika tika = new Tika();

    public UploadResult upload(MultipartFile file) throws Exception {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("file is empty");
        }

        ensureBucketExists(props.getBucket());

        String key = buildKey(file.getOriginalFilename());

        String contentType = resolveContentType(file);

        Map<String, String> meta = baseMetadata(file);

        PutObjectResponse putResp;
        try (InputStream in = file.getInputStream()) {
            PutObjectRequest req = PutObjectRequest.builder()
                    .bucket(props.getBucket())
                    .key(key)
                    .contentType(contentType)
                    .metadata(meta)
                    .build();

            putResp = s3.putObject(req, RequestBody.fromInputStream(in, file.getSize()));
        }

        persistObject(props.getBucket(), key, file, contentType, putResp.eTag(), meta);

        return UploadResult.builder()
                .bucket(props.getBucket())
                .key(key)
                .contentType(contentType)
                .size(file.getSize())
                .originalFilename(file.getOriginalFilename())
                .build();
    }

    public ObjectInfo head(String key) {
        if (key == null || key.isBlank()) throw new IllegalArgumentException("key is empty");

        HeadObjectResponse h = s3.headObject(HeadObjectRequest.builder()
                .bucket(props.getBucket())
                .key(key)
                .build());

        return ObjectInfo.builder()
                .bucket(props.getBucket())
                .key(key)
                .contentType(h.contentType())
                .size(h.contentLength())
                .etag(h.eTag())
                .lastModified(h.lastModified() == null ? null : h.lastModified().toString())
                .metadata(h.metadata() == null ? Map.of() : h.metadata())
                .build();
    }

    public ResponseInputStream<GetObjectResponse> download(String key) {
        if (key == null || key.isBlank()) throw new IllegalArgumentException("key is empty");

        GetObjectRequest req = GetObjectRequest.builder()
                .bucket(props.getBucket())
                .key(key)
                .build();

        return s3.getObject(req);
    }

    public List<ObjectInfo> list(String prefix, Integer maxKeys) {
        int limit = (maxKeys == null || maxKeys <= 0) ? 100 : Math.min(maxKeys, 1000);

        ListObjectsV2Request.Builder b = ListObjectsV2Request.builder()
                .bucket(props.getBucket())
                .maxKeys(limit);

        if (prefix != null && !prefix.isBlank()) b.prefix(prefix);

        ListObjectsV2Response resp = s3.listObjectsV2(b.build());
        List<ObjectInfo> out = new ArrayList<>();

        if (resp.contents() != null) {
            for (S3Object o : resp.contents()) {
                out.add(ObjectInfo.builder()
                        .bucket(props.getBucket())
                        .key(o.key())
                        .size(o.size())
                        .etag(o.eTag())
                        .lastModified(o.lastModified() == null ? null : o.lastModified().toString())
                        .build());
            }
        }
        return out;
    }

    public void delete(String key) {
        if (key == null || key.isBlank()) throw new IllegalArgumentException("key is empty");

        s3.deleteObject(DeleteObjectRequest.builder()
                .bucket(props.getBucket())
                .key(key)
                .build());

        repo.findByObjectKey(key).ifPresent(repo::delete);
    }

    public void copy(String fromKey, String toKey) {
        if (fromKey == null || fromKey.isBlank()) throw new IllegalArgumentException("fromKey is empty");
        if (toKey == null || toKey.isBlank()) throw new IllegalArgumentException("toKey is empty");

        String bucket = props.getBucket();

        s3.copyObject(CopyObjectRequest.builder()
                .destinationBucket(bucket)
                .destinationKey(toKey)
                .copySource(bucket + "/" + fromKey)
                .build());

        repo.findByObjectKey(fromKey).ifPresent(src -> {
            StorageObjectEntity dst = StorageObjectEntity.builder()
                    .id(UUID.randomUUID())
                    .bucket(bucket)
                    .objectKey(toKey)
                    .originalFilename(src.getOriginalFilename())
                    .contentType(src.getContentType())
                    .sizeBytes(src.getSizeBytes())
                    .etag(src.getEtag())
                    .uploadedAt(Instant.now())
                    .metadataJson(src.getMetadataJson())
                    .build();
            repo.save(dst);
        });
    }

    public void move(String fromKey, String toKey) {
        copy(fromKey, toKey);
        delete(fromKey);
    }

    private String resolveContentType(MultipartFile file) {
        String contentType = file.getContentType();
        if (contentType == null || contentType.isBlank()) {
            contentType = tika.detect(file.getOriginalFilename() == null ? "file" : file.getOriginalFilename());
        }
        return contentType;
    }

    private Map<String, String> baseMetadata(MultipartFile file) {
        return Map.of(
                "original-filename", file.getOriginalFilename() == null ? "" : file.getOriginalFilename(),
                "uploaded-at", Instant.now().toString()
        );
    }

    private String buildKey(String originalFilename) {
        String ext = extractExtension(originalFilename);
        return UUID.randomUUID() + ext;
    }

    private String extractExtension(String originalFilename) {
        if (originalFilename == null) return "";
        String name = originalFilename.replace("\\", "/");
        if (name.contains("/")) name = name.substring(name.lastIndexOf('/') + 1);

        int lastDot = name.lastIndexOf('.');
        if (lastDot <= 0 || lastDot == name.length() - 1) return "";

        String ext = name.substring(lastDot).trim();
        // basic sanity: avoid weird/extremely long extensions
        if (ext.length() > 12) return "";
        return ext;
    }

    private void ensureBucketExists(String bucket) {
        try {
            s3.headBucket(HeadBucketRequest.builder().bucket(bucket).build());
        } catch (S3Exception e) {
            s3.createBucket(CreateBucketRequest.builder().bucket(bucket).build());
        }
    }

    private void persistObject(String bucket, String key, MultipartFile file, String contentType, String etag, Map<String,String> meta) {
        StorageObjectEntity entity = StorageObjectEntity.builder()
                .id(UUID.randomUUID())
                .bucket(bucket)
                .objectKey(key)
                .originalFilename(file.getOriginalFilename())
                .contentType(contentType)
                .sizeBytes(file.getSize())
                .etag(etag)
                .uploadedAt(Instant.now())
                .metadataJson(toJson(meta))
                .build();
        repo.save(entity);
    }

    private void upsertObject(String bucket, String key, MultipartFile file, String contentType, String etag, Map<String,String> meta) {
        StorageObjectEntity entity = repo.findByObjectKey(key)
                .map(existing -> {
                    existing.setOriginalFilename(file.getOriginalFilename());
                    existing.setContentType(contentType);
                    existing.setSizeBytes(file.getSize());
                    existing.setEtag(etag);
                    existing.setUploadedAt(Instant.now());
                    existing.setMetadataJson(toJson(meta));
                    return existing;
                })
                .orElseGet(() -> StorageObjectEntity.builder()
                        .id(UUID.randomUUID())
                        .bucket(bucket)
                        .objectKey(key)
                        .originalFilename(file.getOriginalFilename())
                        .contentType(contentType)
                        .sizeBytes(file.getSize())
                        .etag(etag)
                        .uploadedAt(Instant.now())
                        .metadataJson(toJson(meta))
                        .build());

        repo.save(entity);
    }

    private String toJson(Map<String, String> meta) {
        if (meta == null || meta.isEmpty()) return "{}";
        StringBuilder sb = new StringBuilder("{");
        boolean first = true;
        for (var e : meta.entrySet()) {
            if (!first) sb.append(",");
            first = false;
            sb.append("\"").append(escapeJson(e.getKey())).append("\":")
                    .append("\"").append(escapeJson(e.getValue())).append("\"");
        }
        sb.append("}");
        return sb.toString();
    }

    private String escapeJson(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }
}