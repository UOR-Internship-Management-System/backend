package lk.ac.ruhuna.dcs.cvmanagement.modules.academics.persistence.repository;

import jakarta.persistence.LockModeType;
import java.time.OffsetDateTime;
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
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface AcademicLedgerUploadRepository extends JpaRepository<AcademicLedgerUploadEntity, UUID> {

    boolean existsByFileHashAndUploadStatusIn(
            String fileHash, Collection<AcademicLedgerUploadStatus> uploadStatuses);

    Optional<AcademicLedgerUploadEntity> findFirstByFileHashAndUploadStatusIn(
            String fileHash, Collection<AcademicLedgerUploadStatus> uploadStatuses);

    Optional<AcademicLedgerUploadEntity> findFirstByUploadStatusAndValidationStatusOrderByCreatedAtAsc(
            AcademicLedgerUploadStatus uploadStatus, AcademicLedgerValidationStatus validationStatus);

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

    @Query(
            value = """
            SELECT *
            FROM academic.academic_ledger_upload
            WHERE upload_status = 'RECEIVED'
            ORDER BY created_at ASC, academic_ledger_upload_id ASC
            FOR UPDATE SKIP LOCKED
            LIMIT 1
            """,
            nativeQuery = true)
    Optional<AcademicLedgerUploadEntity> findNextReceivedForUpdateSkipLocked();

    @Query(
            value = """
            SELECT *
            FROM academic.academic_ledger_upload
            WHERE upload_status = 'PROCESSING'
              AND processing_started_at IS NOT NULL
              AND updated_at < :staleBefore
            ORDER BY updated_at ASC, academic_ledger_upload_id ASC
            FOR UPDATE SKIP LOCKED
            LIMIT 1
            """,
            nativeQuery = true)
    Optional<AcademicLedgerUploadEntity> findOneStaleProcessingForUpdateSkipLocked(
            @Param("staleBefore") OffsetDateTime staleBefore);

    @Query(
            value = """
            SELECT *
            FROM academic.academic_ledger_upload
            WHERE upload_status = 'STAGED'
              AND validation_status = 'IN_PROGRESS'
              AND updated_at < :staleBefore
            ORDER BY updated_at ASC, academic_ledger_upload_id ASC
            FOR UPDATE SKIP LOCKED
            LIMIT 1
            """,
            nativeQuery = true)
    Optional<AcademicLedgerUploadEntity> findOneStaleValidationForUpdateSkipLocked(
            @Param("staleBefore") OffsetDateTime staleBefore);

    @Query(
            value = """
            SELECT *
            FROM academic.academic_ledger_upload
            WHERE upload_status = 'COMMITTING'
              AND updated_at < :staleBefore
            ORDER BY updated_at ASC, academic_ledger_upload_id ASC
            FOR UPDATE SKIP LOCKED
            LIMIT 1
            """,
            nativeQuery = true)
    Optional<AcademicLedgerUploadEntity> findOneStaleCommittingForUpdateSkipLocked(
            @Param("staleBefore") OffsetDateTime staleBefore);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query(
            value = """
            UPDATE academic.academic_ledger_upload
            SET updated_at = :heartbeat
            WHERE academic_ledger_upload_id = :uploadId AND upload_status = 'PROCESSING'
            """,
            nativeQuery = true)
    int touchProcessingHeartbeat(@Param("uploadId") UUID uploadId, @Param("heartbeat") OffsetDateTime heartbeat);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query(
            value = """
            UPDATE academic.academic_ledger_upload
            SET updated_at = :heartbeat
            WHERE academic_ledger_upload_id = :uploadId
              AND upload_status = 'STAGED'
              AND validation_status = 'IN_PROGRESS'
            """,
            nativeQuery = true)
    int touchValidationHeartbeat(@Param("uploadId") UUID uploadId, @Param("heartbeat") OffsetDateTime heartbeat);
}
