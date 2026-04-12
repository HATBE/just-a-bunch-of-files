package ch.hatbe.jbof.mediaFile;

import ch.hatbe.jbof.mediaFile.entity.MediaFile;
import ch.hatbe.jbof.mediaFile.entity.MediaProcessingStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface MediaFileRepository extends JpaRepository<MediaFile, UUID> {
    @Query(
            value = """
                    select mf
                    from MediaFile mf
                    join mf.metadata md
                    where mf.owner.userId = :ownerUserId
                      and mf.processingStatus = :processingStatus
                    order by coalesce(md.capturedAt, md.uploadedAt) desc,
                             md.uploadedAt desc,
                             mf.createdAt desc
                    """,
            countQuery = """
                    select count(mf)
                    from MediaFile mf
                    join mf.metadata md
                    where mf.owner.userId = :ownerUserId
                      and mf.processingStatus = :processingStatus
                    """
    )
    @EntityGraph(attributePaths = { "owner", "metadata" })
    Page<MediaFile> findAllVisibleForOwner(
            @Param("ownerUserId") UUID ownerUserId,
            @Param("processingStatus") MediaProcessingStatus processingStatus,
            Pageable pageable
    );

    @EntityGraph(attributePaths = { "owner", "metadata", "derivatives", "albums" })
    Optional<MediaFile> findByMediaFileIdAndOwnerUserIdAndProcessingStatus(UUID mediaFileId, UUID ownerUserId, MediaProcessingStatus processingStatus);
}
