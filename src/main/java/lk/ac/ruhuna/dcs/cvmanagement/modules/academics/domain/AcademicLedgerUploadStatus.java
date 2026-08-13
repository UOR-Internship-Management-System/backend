package lk.ac.ruhuna.dcs.cvmanagement.modules.academics.domain;

/** Lifecycle states defined by the Academic Ledger v1.6 API contract. */
public enum AcademicLedgerUploadStatus {
    RECEIVED,
    PROCESSING,
    STAGED,
    READY_TO_COMMIT,
    COMMITTING,
    COMMITTED,
    VALIDATION_FAILED,
    PROCESSING_FAILED
}
