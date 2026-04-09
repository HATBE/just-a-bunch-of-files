package ch.hatbe.jbof.album;

import ch.hatbe.jbof.album.entity.dto.AlbumDetailDto;
import ch.hatbe.jbof.album.entity.dto.AlbumListDto;
import ch.hatbe.jbof.album.entity.requests.CreateAlbumRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/albums")
@RequiredArgsConstructor
public class AlbumController {
    private final AlbumService albumService;

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public List<AlbumListDto> findAll(Pageable pageable) {
        return this.albumService.findAll(pageable);
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<AlbumDetailDto> findById(@PathVariable UUID id) {
        return this.albumService.findById(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping
    @PreAuthorize("hasAuthority('create_album')")
    public ResponseEntity<UUID> create(@Valid @RequestBody CreateAlbumRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(this.albumService.create(request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        this.albumService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
