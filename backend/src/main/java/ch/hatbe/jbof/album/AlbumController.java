package ch.hatbe.jbof.album;

import ch.hatbe.jbof.album.entity.AlbumDtos;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/albums")
@RequiredArgsConstructor
public class AlbumController {
    private final AlbumService service;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public AlbumDtos.ListResponse create(@Valid @RequestBody AlbumDtos.CreateAlbumRequest request) {
        return service.create(request);
    }

    @GetMapping
    public List<AlbumDtos.ListResponse> findAll() {
        return service.findAll();
    }

    @GetMapping("/{albumId}")
    public AlbumDtos.DetailResponse findById(@PathVariable UUID albumId) {
        return service.findById(albumId);
    }

    @PatchMapping("/{albumId}")
    public AlbumDtos.ListResponse rename(
            @PathVariable UUID albumId,
            @Valid @RequestBody AlbumDtos.RenameAlbumRequest request
    ) {
        return service.rename(albumId, request);
    }

    @PostMapping("/{albumId}/files/{fileId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void addFile(@PathVariable UUID albumId, @PathVariable UUID fileId) {
        service.addFile(albumId, fileId);
    }

    @DeleteMapping("/{albumId}/files/{fileId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void removeFile(@PathVariable UUID albumId, @PathVariable UUID fileId) {
        service.removeFile(albumId, fileId);
    }
}
