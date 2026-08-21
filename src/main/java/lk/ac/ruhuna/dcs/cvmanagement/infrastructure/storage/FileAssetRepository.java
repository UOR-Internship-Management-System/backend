package lk.ac.ruhuna.dcs.cvmanagement.infrastructure.storage;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

/** Persistence access for durable file metadata. */
public interface FileAssetRepository extends JpaRepository<FileAssetEntity, UUID> {
    List<FileAssetEntity> findTop100ByStorageKeyStartingWithAndCreatedAtBeforeOrderByCreatedAtAsc(
            String storageKeyPrefix, OffsetDateTime cutoff);
}
