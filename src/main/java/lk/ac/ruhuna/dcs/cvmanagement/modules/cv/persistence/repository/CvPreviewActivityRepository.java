package lk.ac.ruhuna.dcs.cvmanagement.modules.cv.persistence.repository;

import java.util.List;
import java.util.UUID;
import lk.ac.ruhuna.dcs.cvmanagement.modules.cv.persistence.entity.CvPreviewActivityEntity;
import lk.ac.ruhuna.dcs.cvmanagement.modules.cv.persistence.entity.CvPreviewSelectionId;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CvPreviewActivityRepository extends JpaRepository<CvPreviewActivityEntity, CvPreviewSelectionId> {
    List<CvPreviewActivityEntity> findAllByIdPreviewIdOrderByIdSourceRecordIdAsc(UUID previewId);
    void deleteAllByIdPreviewId(UUID previewId);
}
