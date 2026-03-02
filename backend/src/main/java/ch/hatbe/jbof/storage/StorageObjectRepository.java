package ch.hatbe.jbof.storage;

import ch.hatbe.jbof.storage.entity.StorageObjectEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface StorageObjectRepository extends JpaRepository<StorageObjectEntity, UUID> {
    Optional<StorageObjectEntity> findByObjectKey(String objectKey);
    boolean existsByObjectKey(String objectKey);
}