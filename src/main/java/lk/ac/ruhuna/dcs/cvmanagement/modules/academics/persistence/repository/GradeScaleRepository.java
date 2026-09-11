package lk.ac.ruhuna.dcs.cvmanagement.modules.academics.persistence.repository;

import java.util.List;
import java.util.Optional;
import lk.ac.ruhuna.dcs.cvmanagement.modules.academics.persistence.entity.GradeScaleEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GradeScaleRepository extends JpaRepository<GradeScaleEntity, String> {

    Optional<GradeScaleEntity> findByGradeCodeIgnoreCaseAndActiveTrue(String gradeCode);

    List<GradeScaleEntity> findByActiveTrue();
}
