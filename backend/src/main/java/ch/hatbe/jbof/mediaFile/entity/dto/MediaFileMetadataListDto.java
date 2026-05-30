package ch.hatbe.jbof.mediaFile.entity.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import java.time.OffsetDateTime;

@Getter
@Setter
public class MediaFileMetadataListDto {
    @JsonProperty("content_type")
    private String contentType;

    @JsonProperty("captured_at")
    private OffsetDateTime capturedAt;

    @JsonProperty("uploaded_at")
    private OffsetDateTime uploadedAt;
}
