package ch.hatbe.jbof.mediaFile.entity;

import ch.hatbe.jbof.core.entity.AuditableEntity;
import ch.hatbe.jbof.user.entity.User;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UuidGenerator;
import org.hibernate.type.SqlTypes;

import java.util.UUID;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "media_file")
public class MediaFile extends AuditableEntity {
    @Id
    @UuidGenerator
    @Column(name = "media_file_id", nullable = false, updatable = false, length = 36)
    private UUID mediaFileId;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "owner_user_id", nullable = false)
    private User owner;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(name = "kind", nullable = false)
    private MediaFileKind kind;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(name = "processing_state", nullable = false)
    private MediaFileProcessingState processingState;

    @OneToOne(mappedBy = "mediaFile", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private MediaFileThumbnail thumbnail;

    @OneToOne(mappedBy = "mediaFile", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private MediaFileMetadata metadata;

    @Column(name = "storage_bucket", nullable = false, length = 255)
    private String storageBucket;

    @Column(name = "storage_bucket_object_key", nullable = false, length = 1024, unique = true)
    private String storageBucketObjectKey;

    // TODO: albums
}
