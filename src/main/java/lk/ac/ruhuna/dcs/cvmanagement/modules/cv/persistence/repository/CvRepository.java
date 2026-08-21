package lk.ac.ruhuna.dcs.cvmanagement.modules.cv.persistence.repository;

import jakarta.persistence.LockModeType;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lk.ac.ruhuna.dcs.cvmanagement.modules.cv.persistence.entity.CvEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CvRepository extends JpaRepository<CvEntity, UUID> {
    Optional<CvEntity> findByStudentId(UUID studentId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select c from CvEntity c where c.studentId = :studentId")
    Optional<CvEntity> findByStudentIdForUpdate(@Param("studentId") UUID studentId);

    @Query("""
            select c from CvEntity c
            where c.studentId = :studentId
              and c.pdfFileAssetId is not null
              and c.pdfFileSizeBytes is not null
              and c.pdfFileSizeBytes > 0
              and c.sourceFingerprint is not null
            """)
    Optional<CvEntity> findActiveByStudentId(@Param("studentId") UUID studentId);


    @Query("""
            select c from CvEntity c
            where c.studentId in :studentIds
              and c.pdfFileAssetId is not null
              and c.pdfFileSizeBytes is not null
              and c.pdfFileSizeBytes > 0
              and c.sourceFingerprint is not null
            """)
    List<CvEntity> findAllActiveByStudentIdIn(@Param("studentIds") Collection<UUID> studentIds);

    boolean existsByPdfFileAssetId(UUID pdfFileAssetId);
}
