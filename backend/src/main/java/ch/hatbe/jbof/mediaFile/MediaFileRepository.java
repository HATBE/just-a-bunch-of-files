package ch.hatbe.jbof.mediaFile;

import ch.hatbe.jbof.mediaFile.entity.MediaFile;
import ch.hatbe.jbof.mediaFile.entity.MediaProcessingStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface MediaFileRepository extends JpaRepository<MediaFile, UUID> {
    @EntityGraph(attributePaths = { "owner" })
    Page<MediaFile> findAllByOwnerUserIdAndProcessingStatusOrderByMetadataUploadedAtDesc(UUID ownerUserId, MediaProcessingStatus processingStatus, Pageable pageable);

    @EntityGraph(attributePaths = { "owner", "metadata", "derivatives", "albums" })
    Optional<MediaFile> findByMediaFileIdAndOwnerUserIdAndProcessingStatus(UUID mediaFileId, UUID ownerUserId, MediaProcessingStatus processingStatus);
}
