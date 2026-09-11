package lk.ac.ruhuna.dcs.cvmanagement.modules.academics.persistence.repository;

import java.util.List;
import java.util.UUID;
import lk.ac.ruhuna.dcs.cvmanagement.modules.academics.domain.AcademicLedgerRowValidationStatus;
import lk.ac.ruhuna.dcs.cvmanagement.modules.academics.persistence.entity.AcademicLedgerStagingRowEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface AcademicLedgerStagingRowRepository extends
        JpaRepository<AcademicLedgerStagingRowEntity, UUID>,
        JpaSpecificationExecutor<AcademicLedgerStagingRowEntity> {

    List<AcademicLedgerStagingRowEntity> findAllByAcademicLedgerUploadIdOrderByRowNumberAsc(UUID academicLedgerUploadId);

    @Query(
            """
            select r
            from AcademicLedgerStagingRowEntity r
            where r.academicLedgerUploadId = :uploadId
              and r.rowNumber > :afterRowNumber
            order by r.rowNumber asc
            """)
    List<AcademicLedgerStagingRowEntity> findValidationBatch(
            @Param("uploadId") UUID uploadId,
            @Param("afterRowNumber") int afterRowNumber,
            Pageable pageable);

    long countByAcademicLedgerUploadIdAndValidationStatus(
            UUID academicLedgerUploadId, AcademicLedgerRowValidationStatus validationStatus);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("delete from AcademicLedgerStagingRowEntity r where r.academicLedgerUploadId = :uploadId")
    int deleteAllByUploadId(@Param("uploadId") UUID uploadId);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query(
            """
            update AcademicLedgerStagingRowEntity r
            set r.studentId = null,
                r.courseTitle = null,
                r.gradePoint = null,
                r.validationStatus = null
            where r.academicLedgerUploadId = :uploadId
            """)
    int resetValidationArtifacts(@Param("uploadId") UUID uploadId);

    @Query(
            value = """
            SELECT row_number AS "rowNumber", first_row_number AS "relatedRowNumber"
            FROM (
                SELECT row_number,
                       MIN(row_number) OVER (
                           PARTITION BY student_index_number, course_code, semester, academic_year, attempt_number
                       ) AS first_row_number,
                       COUNT(*) OVER (
                           PARTITION BY student_index_number, course_code, semester, academic_year, attempt_number
                       ) AS duplicate_count
                FROM academic.academic_ledger_staging_row
                WHERE academic_ledger_upload_id = :uploadId
            ) duplicate_rows
            WHERE duplicate_count > 1
              AND row_number <> first_row_number
            ORDER BY row_number
            """,
            nativeQuery = true)
    List<DuplicateRowView> findDuplicateRows(@Param("uploadId") UUID uploadId);

    interface DuplicateRowView {
        int getRowNumber();

        int getRelatedRowNumber();
    }
}
