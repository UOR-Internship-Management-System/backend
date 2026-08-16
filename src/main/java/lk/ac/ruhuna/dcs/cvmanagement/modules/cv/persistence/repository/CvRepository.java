package lk.ac.ruhuna.dcs.cvmanagement.modules.cv.persistence.repository;

import java.util.Optional;
import java.util.UUID;
import lk.ac.ruhuna.dcs.cvmanagement.modules.cv.persistence.entity.CvEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CvRepository extends JpaRepository<CvEntity, UUID> {
    Optional<CvEntity> findByStudentId(UUID studentId);
}
