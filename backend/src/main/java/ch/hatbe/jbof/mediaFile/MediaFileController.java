package ch.hatbe.jbof.mediaFile;

import ch.hatbe.jbof.mediaFile.entity.requests.CreateMediaFileRequest;
import ch.hatbe.jbof.mediaFile.entity.dto.MediaFileDetailDto;
import ch.hatbe.jbof.mediaFile.entity.dto.MediaFileListDto;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/media-files")
@RequiredArgsConstructor
public class MediaFileController {
    private final MediaFileService mediaFileService;

    @GetMapping
    @PreAuthorize("hasAuthority('get_mediafile')")
    public Page<MediaFileListDto> findAll(Pageable pageable) {
        return this.mediaFileService.findAll(pageable);
    }

    @GetMapping("/{id}")
    public ResponseEntity<MediaFileDetailDto> findById(@PathVariable UUID id) {
        return this.mediaFileService.findById(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping
    @PreAuthorize("hasAuthority('create_mediafile')")
    public ResponseEntity<List<UUID>> create(@Valid @ModelAttribute CreateMediaFileRequest request) throws Exception {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(this.mediaFileService.create(request));
    }
}
