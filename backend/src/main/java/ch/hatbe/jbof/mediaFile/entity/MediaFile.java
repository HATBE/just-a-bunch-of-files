package ch.hatbe.jbof.mediaFile.entity;

import ch.hatbe.jbof.album.entity.Album;
import ch.hatbe.jbof.user.entity.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UuidGenerator;
import org.hibernate.type.SqlTypes;

import java.time.OffsetDateTime;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "media_files")
public class MediaFile {
    @Id
    @UuidGenerator
    @Column(name = "media_file_id", nullable = false, updatable = false)
    private UUID mediaFileId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "owner_user_id", nullable = false)
    private User owner;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(name = "kind", nullable = false, columnDefinition = "media_kind")
    private MediaKind kind;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(name = "processing_status", nullable = false, columnDefinition = "media_processing_status")
    private MediaProcessingStatus processingStatus;

    @Column(name = "bucket", nullable = false, length = 255)
    private String bucket;

    @Column(name = "object_key", nullable = false, unique = true, length = 1024)
    private String objectKey;

    @Column(name = "original_filename", nullable = false, length = 512)
    private String originalFilename;

    @Column(name = "content_type", nullable = false, length = 255)
    private String contentType;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    @OneToMany(mappedBy = "mediaFile", fetch = FetchType.LAZY)
    private Set<MediaDerivative> derivatives = new LinkedHashSet<>();

    @OneToOne(mappedBy = "mediaFile", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private MediaMetadata metadata;

    @ManyToMany(mappedBy = "mediaFiles", fetch = FetchType.LAZY)
    private Set<Album> albums = new LinkedHashSet<>();
}
