package lk.ac.ruhuna.dcs.cvmanagement.modules.academics.api.dto.response;

import java.util.List;
import java.util.UUID;
import lk.ac.ruhuna.dcs.cvmanagement.modules.academics.domain.AcademicLedgerValidationStatus;

/** Batch validation summary and safe row-level diagnostics. */
public record AcademicLedgerValidationResultResponse(
        UUID uploadId,
        AcademicLedgerValidationStatus validationStatus,
        boolean valid,
        int totalRows,
        int validRows,
        int invalidRows,
        List<AcademicLedgerValidationErrorResponse> errors) {
}
