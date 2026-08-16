package lk.ac.ruhuna.dcs.cvmanagement.modules.cv.persistence.repository;

import java.util.UUID;
import lk.ac.ruhuna.dcs.cvmanagement.modules.cv.persistence.entity.CvSourceFreshnessEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CvSourceFreshnessRepository extends JpaRepository<CvSourceFreshnessEntity, UUID> {
}
