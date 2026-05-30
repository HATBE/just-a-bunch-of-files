package ch.hatbe.jbof.mediaFile.entity;

import ch.hatbe.jbof.core.entity.AuditableEntity;
import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "media_file_thumbnail")
public class MediaFileThumbnail extends AuditableEntity {
    @Id
    @Column(name = "media_file_id", nullable = false, updatable = false, length = 36)
    private UUID mediaFileId; // shares ID with Media File

    @MapsId
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "media_file_id", nullable = false)
    private MediaFile mediaFile;

    @Column(name = "content_type", nullable = false, length = 255)
    private String contentType;

    @Column(name = "width")
    private Integer width;

    @Column(name = "height")
    private Integer height;

    @Column(name = "storage_bucket", nullable = false, length = 255)
    private String storageBucket;

    @Column(name = "storage_bucket_object_key", nullable = false, length = 1024, unique = true)
    private String storageBucketObjectKey;
}
