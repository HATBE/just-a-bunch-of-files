package ch.hatbe.jbof.mediaFile;

import ch.hatbe.jbof.mediaFile.entity.MediaFile;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface MediaFileRepository extends JpaRepository<MediaFile, UUID> {
    @EntityGraph(attributePaths = { "owner" })
    List<MediaFile> findAllByOrderByUploadedAtDesc();

    @EntityGraph(attributePaths = { "owner", "metadata", "derivatives", "albums" })
    Optional<MediaFile> findByMediaFileId(UUID mediaFileId);
}