package lk.ac.ruhuna.dcs.cvmanagement.modules.academics.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/** Transport-level Academic Ledger limits frozen by OpenAPI v1.6.0. */
@ConfigurationProperties(prefix = "app.academics.ledger")
public record AcademicLedgerProperties(long maxFileSizeBytes, int retryAfterSeconds) {

    public AcademicLedgerProperties {
        if (maxFileSizeBytes <= 0) {
            throw new IllegalArgumentException("app.academics.ledger.max-file-size-bytes must be positive.");
        }
        if (retryAfterSeconds < 1 || retryAfterSeconds > 30) {
            throw new IllegalArgumentException("app.academics.ledger.retry-after-seconds must be between 1 and 30.");
        }
    }
}
