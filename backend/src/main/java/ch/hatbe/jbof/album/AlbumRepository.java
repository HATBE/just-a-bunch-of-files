package ch.hatbe.jbof.album;

import ch.hatbe.jbof.album.entity.Album;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AlbumRepository extends JpaRepository<Album, UUID> {
    @EntityGraph(attributePaths = { "owner" })
    List<Album> findAllByOrderByCreatedAtDesc();

    @EntityGraph(attributePaths = { "owner", "mediaFiles" })
    Optional<Album> findByAlbumId(UUID albumId);

    boolean existsByAlbumIdAndOwnerUserId(UUID albumId, UUID ownerUserId);
}
