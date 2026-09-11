package lk.ac.ruhuna.dcs.cvmanagement.modules.exports.persistence.repository;

import java.util.List;
import java.util.UUID;
import lk.ac.ruhuna.dcs.cvmanagement.modules.exports.persistence.entity.ExportFileEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ExportFileRepository extends JpaRepository<ExportFileEntity, ExportFileEntity.Id> {
    List<ExportFileEntity> findAllByExportJobIdOrderByIndexNumberAscStudentIdAsc(UUID exportJobId);
}
