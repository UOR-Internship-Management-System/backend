package lk.ac.ruhuna.dcs.cvmanagement.modules.academics.persistence.repository;

import java.util.UUID;
import lk.ac.ruhuna.dcs.cvmanagement.modules.academics.persistence.entity.StudentAcademicSummaryEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StudentAcademicSummaryRepository extends JpaRepository<StudentAcademicSummaryEntity, UUID> {
}
