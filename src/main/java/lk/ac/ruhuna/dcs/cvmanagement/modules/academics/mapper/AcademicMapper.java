package lk.ac.ruhuna.dcs.cvmanagement.modules.academics.mapper;

import java.util.List;
import lk.ac.ruhuna.dcs.cvmanagement.modules.academics.api.dto.response.AcademicLedgerStagedRowResponse;
import lk.ac.ruhuna.dcs.cvmanagement.modules.academics.api.dto.response.AcademicLedgerValidationErrorResponse;
import lk.ac.ruhuna.dcs.cvmanagement.modules.academics.domain.AcademicLedgerValidationSeverity;
import lk.ac.ruhuna.dcs.cvmanagement.modules.academics.persistence.entity.AcademicLedgerStagingRowEntity;
import lk.ac.ruhuna.dcs.cvmanagement.modules.academics.persistence.entity.AcademicLedgerValidationErrorEntity;
import lk.ac.ruhuna.dcs.cvmanagement.modules.academics.persistence.repository.AcademicLedgerValidationErrorRepository;
import org.springframework.stereotype.Component;

/** Explicit Academic Ledger persistence-to-API mapper. */
@Component
public class AcademicMapper {

    public AcademicLedgerStagedRowResponse toStagedRow(
            AcademicLedgerStagingRowEntity row,
            List<AcademicLedgerValidationErrorEntity> errors) {
        return new AcademicLedgerStagedRowResponse(
                row.getId(),
                row.getAcademicLedgerUploadId(),
                row.getRowNumber(),
                row.getStudentIndexNumber(),
                row.getStudentId(),
                row.getCourseCode(),
                row.getCourseTitle(),
                row.getCredits(),
                row.getLetterGrade(),
                row.getGradePoint(),
                row.getSemester(),
                row.getAcademicYear(),
                row.getAttemptNumber(),
                row.getResultStatus(),
                row.getValidationStatus(),
                errors.stream().map(error -> toValidationError(row.getRowNumber(), error)).toList());
    }

    public AcademicLedgerValidationErrorResponse toValidationError(
            AcademicLedgerValidationErrorRepository.ValidationErrorView view) {
        return new AcademicLedgerValidationErrorResponse(
                view.getRowNumber(),
                view.getFieldName(),
                view.getErrorCode(),
                view.getErrorMessage(),
                AcademicLedgerValidationSeverity.valueOf(view.getSeverity()),
                view.getRejectedValue(),
                view.getRelatedRowNumber());
    }

    private AcademicLedgerValidationErrorResponse toValidationError(
            int rowNumber, AcademicLedgerValidationErrorEntity error) {
        return new AcademicLedgerValidationErrorResponse(
                rowNumber,
                error.getFieldName(),
                error.getErrorCode(),
                error.getErrorMessage(),
                error.getSeverity(),
                error.getRejectedValue(),
                error.getRelatedRowNumber());
    }
}
