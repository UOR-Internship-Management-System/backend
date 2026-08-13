package lk.ac.ruhuna.dcs.cvmanagement.modules.academics.persistence.repository;

import java.util.Collection;
import java.util.List;
import java.util.UUID;
import lk.ac.ruhuna.dcs.cvmanagement.modules.academics.persistence.entity.AcademicLedgerValidationErrorEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AcademicLedgerValidationErrorRepository
        extends JpaRepository<AcademicLedgerValidationErrorEntity, UUID> {

    List<AcademicLedgerValidationErrorEntity> findByStagingRowIdInOrderByCreatedAtAsc(
            Collection<UUID> stagingRowIds);
}
