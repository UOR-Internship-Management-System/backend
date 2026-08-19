package lk.ac.ruhuna.dcs.cvmanagement.modules.cv.persistence.repository;

import java.util.List;
import java.util.UUID;
import lk.ac.ruhuna.dcs.cvmanagement.modules.cv.persistence.entity.CvSelectedAwardEntity;
import lk.ac.ruhuna.dcs.cvmanagement.modules.cv.persistence.entity.CvActiveSelectionId;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CvSelectedAwardRepository extends JpaRepository<CvSelectedAwardEntity, CvActiveSelectionId> {
    List<CvSelectedAwardEntity> findAllByIdCvIdOrderByIdSourceRecordIdAsc(UUID cvId);
    void deleteAllByIdCvId(UUID cvId);
}
