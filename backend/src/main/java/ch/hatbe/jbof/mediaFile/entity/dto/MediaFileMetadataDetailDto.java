package ch.hatbe.jbof.mediaFile.entity.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import java.time.OffsetDateTime;

@Getter
@Setter
public class MediaFileMetadataDetailDto {
    @JsonProperty("original_filename")
    private String originalFilename;

    @JsonProperty("content_type")
    private String contentType;

    @JsonProperty("gps_lat")
    private Double gpsLat;

    @JsonProperty("gps_lon")
    private Double gpsLon;

    @JsonProperty("camera_make")
    private String cameraMake;

    @JsonProperty("camera_model")
    private String cameraModel;

    @JsonProperty("size_bytes")
    private Long sizeBytes;

    @JsonProperty("captured_at")
    private OffsetDateTime capturedAt;

    @JsonProperty("uploaded_at")
    private OffsetDateTime uploadedAt;

    @JsonProperty("width")
    private Integer width;

    @JsonProperty("height")
    private Integer height;

    @JsonProperty("duration_ms")
    private Long durationMs;

    @JsonProperty("metadata_json")
    private String metadataJson;
}
