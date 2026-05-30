package ch.hatbe.jbof.mediaFile.entity.dto;

import ch.hatbe.jbof.album.entity.Album;
import ch.hatbe.jbof.mediaFile.entity.MediaFileKind;
import ch.hatbe.jbof.user.entity.dto.UserListDto;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import java.util.Set;
import java.util.UUID;

@Getter
@Setter
public class MediaFileDetailDto {
    @JsonProperty("media_file_id")
    private UUID mediaFileId;

    @JsonProperty("owner")
    private UserListDto owner;

    @JsonProperty("kind")
    private MediaFileKind kind;

    @JsonProperty("thumbnail")
    private MediaFileThumbnailDto thumbnail;

    @JsonProperty("metadata")
    private MediaFileMetadataDetailDto metadata;

    @JsonProperty("albums")
    private Set<Album> albums;

    @JsonProperty("image_url")
    public String getImageUrl() {
        return "/api/v1/media-files/" + this.mediaFileId + "/content";
    }
}
