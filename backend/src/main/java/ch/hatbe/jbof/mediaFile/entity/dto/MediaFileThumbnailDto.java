package ch.hatbe.jbof.mediaFile.entity.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class MediaFileThumbnailDto {
    @JsonIgnore
    private UUID mediaFileId;

    @JsonProperty("content_type")
    private String contentType;

    @JsonProperty("width")
    private Integer width;

    @JsonProperty("height")
    private Integer height;

    @JsonProperty("image_url")
    public String getImageUrl() {
        return "/api/v1/media-files/" + mediaFileId + "/thumbnail";
    }
}