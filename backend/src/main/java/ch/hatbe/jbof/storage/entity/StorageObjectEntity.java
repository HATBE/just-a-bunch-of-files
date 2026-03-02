package ch.hatbe.jbof.storage.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "storage_object")
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StorageObjectEntity {
    @Id
    private UUID id;

    @Column(nullable = false)
    private String bucket;

    @Column(name = "object_key", nullable = false, unique = true, length = 1024)
    private String objectKey;

    @Column(name = "original_filename", length = 1024)
    private String originalFilename;

    @Column(name = "content_type")
    private String contentType;

    @Column(name = "size_bytes")
    private Long sizeBytes;

    @Column(name = "etag")
    private String etag;

    @Column(name = "uploaded_at", nullable = false)
    private Instant uploadedAt;

    @Column(name = "metadata_json", nullable = false, columnDefinition = "text")
    private String metadataJson;
}