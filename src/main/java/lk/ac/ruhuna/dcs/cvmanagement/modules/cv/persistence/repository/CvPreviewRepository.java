package lk.ac.ruhuna.dcs.cvmanagement.modules.cv.persistence.repository;

import jakarta.persistence.LockModeType;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lk.ac.ruhuna.dcs.cvmanagement.modules.cv.persistence.entity.CvPreviewEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CvPreviewRepository extends JpaRepository<CvPreviewEntity, UUID> {
    Optional<CvPreviewEntity> findByPreviewIdAndStudentId(UUID previewId, UUID studentId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select p from CvPreviewEntity p where p.previewId = :previewId and p.studentId = :studentId")
    Optional<CvPreviewEntity> findOwnedForUpdate(@Param("previewId") UUID previewId, @Param("studentId") UUID studentId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    List<CvPreviewEntity> findTop100ByConsumedAtIsNullAndExpiresAtBeforeOrderByExpiresAtAsc(OffsetDateTime cutoff);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    List<CvPreviewEntity> findTop100ByConsumedAtIsNotNullAndConsumedAtBeforeOrderByConsumedAtAsc(OffsetDateTime cutoff);
}
