package ch.hatbe.jbof.mediaFile.entity;

import jakarta.persistence.*;
import lombok.Getter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Getter
@Table(
        name = "media_derivatives",
        uniqueConstraints = {
                @UniqueConstraint(name = "uq_media_derivatives_variant", columnNames = {
                        "media_file_id", "kind", "width", "height"
                })
        }
)
public class MediaDerivative {
    @Id
    @Column(name = "derivative_id", nullable = false, updatable = false)
    private UUID derivativeId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "media_file_id", nullable = false)
    private MediaFile mediaFile;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(name = "kind", nullable = false, columnDefinition = "media_derivative_kind")
    private MediaDerivativeKind kind;

    @Column(name = "bucket", nullable = false, length = 255)
    private String bucket;

    @Column(name = "object_key", nullable = false, unique = true, length = 1024)
    private String objectKey;

    @Column(name = "content_type", nullable = false, length = 255)
    private String contentType;

    @Column(name = "width", nullable = false)
    private Integer width;

    @Column(name = "height", nullable = false)
    private Integer height;

    @Column(name = "size_bytes")
    private Long sizeBytes;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;
}
