package lk.ac.ruhuna.dcs.cvmanagement.modules.academics.config;

import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;

/** Bounded worker and recovery settings for durable Academic Ledger processing. */
@ConfigurationProperties(prefix = "app.academics.ledger.processing")
public record AcademicLedgerProcessingProperties(
        int stagingBatchSize,
        int maxUploadsPerPoll,
        Duration staleThreshold) {

    public AcademicLedgerProcessingProperties {
        if (stagingBatchSize < 50 || stagingBatchSize > 2_000) {
            throw new IllegalArgumentException(
                    "app.academics.ledger.processing.staging-batch-size must be between 50 and 2000.");
        }
        if (maxUploadsPerPoll < 1 || maxUploadsPerPoll > 10) {
            throw new IllegalArgumentException(
                    "app.academics.ledger.processing.max-uploads-per-poll must be between 1 and 10.");
        }
        if (staleThreshold == null || staleThreshold.isNegative() || staleThreshold.isZero()) {
            throw new IllegalArgumentException(
                    "app.academics.ledger.processing.stale-threshold must be positive.");
        }
    }
}
