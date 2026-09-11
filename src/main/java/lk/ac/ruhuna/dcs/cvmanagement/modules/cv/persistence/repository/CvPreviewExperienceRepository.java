package lk.ac.ruhuna.dcs.cvmanagement.modules.cv.persistence.repository;

import java.util.List;
import java.util.UUID;
import lk.ac.ruhuna.dcs.cvmanagement.modules.cv.persistence.entity.CvPreviewExperienceEntity;
import lk.ac.ruhuna.dcs.cvmanagement.modules.cv.persistence.entity.CvPreviewSelectionId;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CvPreviewExperienceRepository extends JpaRepository<CvPreviewExperienceEntity, CvPreviewSelectionId> {
    List<CvPreviewExperienceEntity> findAllByIdPreviewIdOrderByIdSourceRecordIdAsc(UUID previewId);
    void deleteAllByIdPreviewId(UUID previewId);
}
