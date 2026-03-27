package ch.hatbe.jbof.media;

import ch.hatbe.jbof.media.entity.MediaDtos;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/media")
@RequiredArgsConstructor
public class MediaController {
    private final MediaService service;

    @PostMapping
    public List<MediaDtos.DetailResponse> upload(
            @RequestParam UUID userId,
            @RequestParam(required = false) List<UUID> albumIds,
            @RequestParam("files") List<MultipartFile> files
    ) throws IOException {
        return service.upload(userId, albumIds, files);
    }

    @GetMapping
    public List<MediaDtos.ListResponse> findAll(@RequestParam(required = false) UUID userId) {
        return service.findAll(userId);
    }

    @GetMapping("/{fileId}")
    public MediaDtos.DetailResponse findById(@PathVariable UUID fileId) {
        return service.findById(fileId);
    }

    @GetMapping("/{fileId}/content")
    public ResponseEntity<InputStreamResource> download(@PathVariable UUID fileId) {
        MediaDtos.DetailResponse file = service.findById(fileId);
        ResponseInputStream<GetObjectResponse> objectStream = service.download(fileId);

        String contentType = file.contentType() == null || file.contentType().isBlank()
                ? MediaType.APPLICATION_OCTET_STREAM_VALUE
                : file.contentType();

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + file.originalFilename() + "\"")
                .contentType(MediaType.parseMediaType(contentType))
                .contentLength(file.sizeBytes())
                .body(new InputStreamResource(objectStream));
    }

    @DeleteMapping("/{fileId}")
    public ResponseEntity<Void> delete(@PathVariable UUID fileId) {
        service.delete(fileId);
        return ResponseEntity.noContent().build();
    }
}
