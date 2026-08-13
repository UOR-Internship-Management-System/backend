package lk.ac.ruhuna.dcs.cvmanagement.modules.academics.domain;

/** Batch-level validation lifecycle, independent from file-processing status. */
public enum AcademicLedgerValidationStatus {
    NOT_STARTED,
    IN_PROGRESS,
    PASSED,
    FAILED
}
