package lk.ac.ruhuna.dcs.cvmanagement.modules.exports.config;

import java.nio.file.Path;
import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;

/** Strongly typed export storage and worker limits. */
@ConfigurationProperties(prefix = "app.exports")
public record ExportProperties(Storage storage, Processing processing) {

    public ExportProperties {
        if (storage == null || storage.root() == null) {
            throw new IllegalArgumentException("app.exports.storage.root must be configured.");
        }
        if (processing == null
                || processing.maxJobsPerPoll() < 1
                || processing.maxJobsPerPoll() > 20
                || processing.retention() == null
                || processing.retention().isNegative()
                || processing.retention().isZero()) {
            throw new IllegalArgumentException("Invalid app.exports.processing configuration.");
        }
    }

    public record Storage(Path root) {}

    public record Processing(
            boolean workerEnabled,
            long workerPollDelayMs,
            int maxJobsPerPoll,
            Duration retention) {}
}
