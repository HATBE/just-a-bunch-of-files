package ch.hatbe.jbof.mediaFile;

import ch.hatbe.jbof.mediaFile.entity.dto.MediaFileDetailDto;
import ch.hatbe.jbof.mediaFile.entity.dto.MediaFileListDto;
import ch.hatbe.jbof.user.entity.dto.UserDetailDto;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/mediafiles")
public class MediaFileController {
    private final MediaFileService mediaFileService;

    @GetMapping
    public Page<MediaFileListDto> findAll(Pageable pageable) {
        return this.mediaFileService.findAll(pageable);
    }

    @GetMapping("/{id}")
    public ResponseEntity<MediaFileDetailDto> findById(@PathVariable UUID id) {
        return this.mediaFileService.findById(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
}
