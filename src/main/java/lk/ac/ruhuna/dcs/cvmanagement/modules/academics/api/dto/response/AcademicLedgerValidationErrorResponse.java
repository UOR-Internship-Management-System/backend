package lk.ac.ruhuna.dcs.cvmanagement.modules.academics.api.dto.response;

import lk.ac.ruhuna.dcs.cvmanagement.modules.academics.domain.AcademicLedgerValidationSeverity;

/** Safe row-level Academic Ledger validation diagnostic exposed by v1.6. */
public record AcademicLedgerValidationErrorResponse(
        int rowNumber,
        String field,
        String code,
        String message,
        AcademicLedgerValidationSeverity severity,
        String rejectedValue,
        Integer relatedRowNumber) {
}
