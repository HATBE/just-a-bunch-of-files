package ch.hatbe.jbof.album;

import ch.hatbe.jbof.album.entity.dto.AlbumDetailDto;
import ch.hatbe.jbof.album.entity.dto.AlbumListDto;
import ch.hatbe.jbof.album.entity.request.CreateAlbumRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/albums")
public class AlbumController {
    private final AlbumService albumService;

    @PostMapping
    public ResponseEntity<UUID> create(@Valid @RequestBody CreateAlbumRequest req) throws URISyntaxException {
        UUID id = this.albumService.create(req);
        return ResponseEntity.created(new URI("/api/v1/albums/" + id)).body(id);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        this.albumService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    public List<AlbumListDto> findAll(Pageable pageable) {
        return this.albumService.findAll(pageable);
    }

    @GetMapping("/{id}")
    public ResponseEntity<AlbumDetailDto> findById(@PathVariable UUID id) {
        return this.albumService.findById(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
}
