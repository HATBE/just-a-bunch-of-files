package ch.hatbe.jbof.mediaFile;

import ch.hatbe.jbof.mediaFile.entity.MediaFile;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface MediaFileRepository extends JpaRepository<MediaFile, UUID> {

    @Query(value = """
        SELECT m FROM MediaFile m
            WHERE m.deletedAt IS NULL
            AND m.processingState = MediaFileProcessingState.READY
            ORDER BY m.metadata.capturedAt DESC, m.updatedAt DESC, m.createdAt DESC
    """)
    Page<MediaFile> findAllMediaFiles(Pageable pageable);

    @Query(value = """
        SELECT m FROM MediaFile m
            WHERE m.deletedAt IS NULL
            AND m.mediaFileId = :id
    """)
    Optional<MediaFile> findMediaFileById(UUID id);
}
