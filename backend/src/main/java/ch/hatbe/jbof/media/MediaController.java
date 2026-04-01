package ch.hatbe.jbof.media;

import ch.hatbe.jbof.media.entity.MediaDtos;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/media-files")
@RequiredArgsConstructor
public class MediaController {
    private final MediaService service;

    @GetMapping
    public List<MediaDtos.MediaFileListResponse> getAll(
            @RequestParam(required = false) UUID albumId
    ) {
        if (albumId != null) {
            return service.getAllForAlbum(albumId);
        }

        return service.getAllForList();
    }

    @GetMapping("/{mediaFileId}")
    public MediaDtos.MediaFileDetailedResponse getById(@PathVariable UUID mediaFileId) {
        return service.getDetailedById(mediaFileId);
    }
}
