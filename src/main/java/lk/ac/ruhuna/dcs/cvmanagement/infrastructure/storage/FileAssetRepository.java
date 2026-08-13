package lk.ac.ruhuna.dcs.cvmanagement.infrastructure.storage;

import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

/** Persistence access for durable file metadata. */
public interface FileAssetRepository extends JpaRepository<FileAssetEntity, UUID> {
}
