package lk.ac.ruhuna.dcs.cvmanagement.modules.exports.persistence.repository;

import jakarta.persistence.LockModeType;
import java.util.Collection;
import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;
import lk.ac.ruhuna.dcs.cvmanagement.modules.exports.domain.policy.ExportStatus;
import lk.ac.ruhuna.dcs.cvmanagement.modules.exports.domain.policy.ExportType;
import lk.ac.ruhuna.dcs.cvmanagement.modules.exports.persistence.entity.ExportJobEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;

public interface ExportJobRepository extends JpaRepository<ExportJobEntity, UUID> {
    boolean existsByShortlistIdAndExportTypeAndStatusIn(UUID shortlistId, ExportType exportType, Collection<ExportStatus> statuses);
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<ExportJobEntity> findFirstByStatusOrderByCreatedAtAscIdAsc(ExportStatus status);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<ExportJobEntity> findFirstByStatusAndStartedAtBeforeOrderByStartedAtAscIdAsc(
            ExportStatus status, OffsetDateTime cutoff);
}
