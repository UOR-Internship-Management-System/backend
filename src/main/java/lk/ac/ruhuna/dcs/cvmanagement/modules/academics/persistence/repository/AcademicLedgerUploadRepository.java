package lk.ac.ruhuna.dcs.cvmanagement.modules.academics.persistence.repository;

import jakarta.persistence.LockModeType;
import java.util.Collection;
import java.util.Optional;
import java.util.UUID;
import lk.ac.ruhuna.dcs.cvmanagement.modules.academics.domain.AcademicLedgerUploadStatus;
import lk.ac.ruhuna.dcs.cvmanagement.modules.academics.domain.AcademicLedgerValidationStatus;
import lk.ac.ruhuna.dcs.cvmanagement.modules.academics.persistence.entity.AcademicLedgerUploadEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface AcademicLedgerUploadRepository extends JpaRepository<AcademicLedgerUploadEntity, UUID> {

    boolean existsByFileHashAndUploadStatusIn(
            String fileHash, Collection<AcademicLedgerUploadStatus> uploadStatuses);

    Optional<AcademicLedgerUploadEntity> findFirstByFileHashAndUploadStatusIn(
            String fileHash, Collection<AcademicLedgerUploadStatus> uploadStatuses);

    @Query(
            """
            select u
            from AcademicLedgerUploadEntity u
            where (:search is null or locate(lower(:search), lower(u.fileName)) > 0)
              and (:status is null or u.uploadStatus = :status)
              and (:validationStatus is null or u.validationStatus = :validationStatus)
            """)
    Page<AcademicLedgerUploadEntity> searchUploads(
            @Param("search") String search,
            @Param("status") AcademicLedgerUploadStatus status,
            @Param("validationStatus") AcademicLedgerValidationStatus validationStatus,
            Pageable pageable);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select u from AcademicLedgerUploadEntity u where u.id = :uploadId")
    Optional<AcademicLedgerUploadEntity> findByIdForUpdate(@Param("uploadId") UUID uploadId);
}
