package lk.ac.ruhuna.dcs.cvmanagement.modules.academics.api.dto.response;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import lk.ac.ruhuna.dcs.cvmanagement.modules.academics.domain.AcademicLedgerRowValidationStatus;

/** Read-only normalized staging row. Staged data is never official academic data. */
public record AcademicLedgerStagedRowResponse(
        UUID stagingRowId,
        UUID uploadId,
        int rowNumber,
        String studentIndexNumber,
        UUID studentId,
        String courseCode,
        String courseTitle,
        BigDecimal credits,
        String letterGrade,
        BigDecimal gradePoint,
        String semester,
        String academicYear,
        int attemptNumber,
        String resultStatus,
        AcademicLedgerRowValidationStatus validationStatus,
        List<AcademicLedgerValidationErrorResponse> validationErrors) {
}
