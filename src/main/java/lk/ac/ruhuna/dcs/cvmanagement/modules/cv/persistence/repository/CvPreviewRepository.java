package lk.ac.ruhuna.dcs.cvmanagement.modules.cv.persistence.repository;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lk.ac.ruhuna.dcs.cvmanagement.modules.cv.persistence.entity.CvPreviewEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CvPreviewRepository extends JpaRepository<CvPreviewEntity, UUID> {
    Optional<CvPreviewEntity> findByPreviewIdAndStudentId(UUID previewId, UUID studentId);
    List<CvPreviewEntity> findTop100ByConsumedAtIsNullAndExpiresAtBeforeOrderByExpiresAtAsc(OffsetDateTime cutoff);
}
