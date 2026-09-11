package lk.ac.ruhuna.dcs.cvmanagement.modules.exports.domain.policy;

/** Durable asynchronous export lifecycle. */
public enum ExportStatus {
    QUEUED,
    PROCESSING,
    COMPLETED,
    FAILED,
    CANCELLED
}
