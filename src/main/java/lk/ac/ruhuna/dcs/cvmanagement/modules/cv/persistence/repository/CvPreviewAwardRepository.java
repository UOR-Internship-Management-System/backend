package lk.ac.ruhuna.dcs.cvmanagement.modules.cv.persistence.repository;

import java.util.List;
import java.util.UUID;
import lk.ac.ruhuna.dcs.cvmanagement.modules.cv.persistence.entity.CvPreviewAwardEntity;
import lk.ac.ruhuna.dcs.cvmanagement.modules.cv.persistence.entity.CvPreviewSelectionId;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CvPreviewAwardRepository extends JpaRepository<CvPreviewAwardEntity, CvPreviewSelectionId> {
    List<CvPreviewAwardEntity> findAllByIdPreviewIdOrderByIdSourceRecordIdAsc(UUID previewId);
    void deleteAllByIdPreviewId(UUID previewId);
}
