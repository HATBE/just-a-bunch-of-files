package ch.hatbe.jbof.album;

import ch.hatbe.jbof.album.entity.AlbumDetailDto;
import ch.hatbe.jbof.album.entity.AlbumListDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/albums")
@RequiredArgsConstructor
public class AlbumController {
    private final AlbumService albumService;

    @GetMapping
    public List<AlbumListDto> findAll() {
        return albumService.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<AlbumDetailDto> findById(@PathVariable UUID id) {
        return albumService.findById(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
}