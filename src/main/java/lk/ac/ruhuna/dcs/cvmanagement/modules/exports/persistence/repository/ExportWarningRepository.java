package lk.ac.ruhuna.dcs.cvmanagement.modules.exports.persistence.repository;

import java.util.List;
import java.util.UUID;
import lk.ac.ruhuna.dcs.cvmanagement.modules.exports.persistence.entity.ExportWarningEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ExportWarningRepository extends JpaRepository<ExportWarningEntity, ExportWarningEntity.Id> {
    List<ExportWarningEntity> findAllByExportJobIdOrderByWarningCodeAsc(UUID exportJobId);
}
