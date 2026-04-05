package ch.hatbe.jbof.mediaFile.entity;

import jakarta.persistence.*;
import lombok.Getter;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Getter
@Table(name = "media_metadata")
public class MediaMetadata {
    @Id
    @Column(name = "media_file_id", nullable = false, updatable = false)
    private UUID mediaFileId;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @MapsId
    @JoinColumn(name = "media_file_id")
    private MediaFile mediaFile;

    @Column(name = "gps_lat")
    private Double gpsLat;

    @Column(name = "gps_lon")
    private Double gpsLon;

    @Column(name = "camera_make")
    private String cameraMake;

    @Column(name = "camera_model")
    private String cameraModel;

    @Column(name = "metadata_json", nullable = false, columnDefinition = "jsonb")
    private String metadataJson;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;
}
