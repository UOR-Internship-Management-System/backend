package lk.ac.ruhuna.dcs.cvmanagement.modules.academics.application;

/** Internal exception for source corruption or durable processing failures after a 202 acceptance. */
final class AcademicLedgerProcessingException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    AcademicLedgerProcessingException(String message, Throwable cause) {
        super(message, cause);
    }

    AcademicLedgerProcessingException(String message) {
        super(message);
    }
}
