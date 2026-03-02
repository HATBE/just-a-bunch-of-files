package ch.hatbe.jbof.storage;

import ch.hatbe.jbof.storage.entity.UploadResult;
import ch.hatbe.jbof.storage.entity.ObjectInfo;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

@RestController
@RequestMapping("/api/v1/files")
@RequiredArgsConstructor
public class StorageObjectsController {
    private final StorageService storageService;

    @PostMapping("/")
    public ResponseEntity<UploadResult> upload(@RequestParam("file") MultipartFile file) throws Exception {
        return ResponseEntity.ok(storageService.upload(file));
    }

    @GetMapping("/{key}/meta")
    public ResponseEntity<ObjectInfo> getMeta(@PathVariable String key) {
        return ResponseEntity.ok(storageService.head(key));
    }

    @GetMapping("/{key}")
    public ResponseEntity<InputStreamResource> download(
            @PathVariable String key,
            @RequestParam(defaultValue = "true") boolean download
    ) {
        ResponseInputStream<GetObjectResponse> stream = storageService.download(key);
        GetObjectResponse response = stream.response();

        String filename = key;
        String encodedName = URLEncoder.encode(filename, StandardCharsets.UTF_8);

        String disposition =
                (download ? "attachment" : "inline") +
                        "; filename*=UTF-8''" + encodedName;

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_TYPE,
                        response.contentType() == null
                                ? "application/octet-stream"
                                : response.contentType())
                .header(HttpHeaders.CONTENT_DISPOSITION, disposition)
                .contentLength(response.contentLength())
                .body(new InputStreamResource(stream));
    }

    @GetMapping
    public ResponseEntity<List<ObjectInfo>> list(
            @RequestParam(required = false) String prefix,
            @RequestParam(required = false) Integer max
    ) {
        return ResponseEntity.ok(storageService.list(prefix, max));
    }

    @DeleteMapping("/{key}")
    public ResponseEntity<Void> delete(@PathVariable String key) {
        storageService.delete(key);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/copy")
    public ResponseEntity<Void> copy(
            @RequestParam String from,
            @RequestParam String to
    ) {
        storageService.copy(from, to);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/move")
    public ResponseEntity<Void> move(
            @RequestParam String from,
            @RequestParam String to
    ) {
        storageService.move(from, to);
        return ResponseEntity.noContent().build();
    }
}