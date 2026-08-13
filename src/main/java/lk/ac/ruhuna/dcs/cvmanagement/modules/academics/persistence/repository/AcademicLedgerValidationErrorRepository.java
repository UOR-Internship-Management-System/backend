package lk.ac.ruhuna.dcs.cvmanagement.modules.academics.persistence.repository;

import java.util.Collection;
import java.util.List;
import java.util.UUID;
import lk.ac.ruhuna.dcs.cvmanagement.modules.academics.persistence.entity.AcademicLedgerValidationErrorEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface AcademicLedgerValidationErrorRepository
        extends JpaRepository<AcademicLedgerValidationErrorEntity, UUID> {

    List<AcademicLedgerValidationErrorEntity> findByStagingRowIdInOrderByCreatedAtAsc(
            Collection<UUID> stagingRowIds);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query(
            value = """
            DELETE FROM academic.academic_ledger_validation_error e
            USING academic.academic_ledger_staging_row r
            WHERE e.staging_row_id = r.staging_row_id
              AND r.academic_ledger_upload_id = :uploadId
            """,
            nativeQuery = true)
    int deleteAllByUploadId(@Param("uploadId") UUID uploadId);

    @Query(
            value = """
            SELECT r.row_number AS "rowNumber",
                   e.field_name AS "fieldName",
                   e.error_code AS "errorCode",
                   e.error_message AS "errorMessage",
                   e.severity AS "severity",
                   e.rejected_value AS "rejectedValue",
                   e.related_row_number AS "relatedRowNumber"
            FROM academic.academic_ledger_validation_error e
            JOIN academic.academic_ledger_staging_row r ON r.staging_row_id = e.staging_row_id
            WHERE r.academic_ledger_upload_id = :uploadId
            ORDER BY r.row_number ASC, e.created_at ASC, e.validation_error_id ASC
            """,
            nativeQuery = true)
    List<ValidationErrorView> findValidationResultRows(@Param("uploadId") UUID uploadId);

    interface ValidationErrorView {
        int getRowNumber();

        String getFieldName();

        String getErrorCode();

        String getErrorMessage();

        String getSeverity();

        String getRejectedValue();

        Integer getRelatedRowNumber();
    }
}
