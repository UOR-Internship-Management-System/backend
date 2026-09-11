package lk.ac.ruhuna.dcs.cvmanagement.modules.cv.persistence.repository;

import java.util.List;
import java.util.UUID;
import lk.ac.ruhuna.dcs.cvmanagement.modules.cv.persistence.entity.CvPreviewProjectEntity;
import lk.ac.ruhuna.dcs.cvmanagement.modules.cv.persistence.entity.CvPreviewSelectionId;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CvPreviewProjectRepository extends JpaRepository<CvPreviewProjectEntity, CvPreviewSelectionId> {
    List<CvPreviewProjectEntity> findAllByIdPreviewIdOrderByIdSourceRecordIdAsc(UUID previewId);
    void deleteAllByIdPreviewId(UUID previewId);
}
