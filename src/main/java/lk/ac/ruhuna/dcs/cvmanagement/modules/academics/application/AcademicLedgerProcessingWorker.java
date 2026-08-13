package lk.ac.ruhuna.dcs.cvmanagement.modules.academics.application;

import java.time.Clock;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import lk.ac.ruhuna.dcs.cvmanagement.modules.academics.config.AcademicLedgerProcessingProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Database-backed Academic Ledger worker.
 *
 * <p>The database row is the durable queue. JVM memory is never the sole owner of accepted work.
 */
@Component
@ConditionalOnProperty(
        name = "app.academics.ledger.processing.worker-enabled",
        havingValue = "true",
        matchIfMissing = true)
class AcademicLedgerProcessingWorker {

    private static final Logger LOGGER = LoggerFactory.getLogger(AcademicLedgerProcessingWorker.class);

    private final AcademicLedgerProcessingStateService stateService;
    private final AcademicLedgerProcessingService processingService;
    private final AcademicLedgerValidationService validationService;
    private final AcademicLedgerProcessingProperties properties;
    private final Clock clock;

    AcademicLedgerProcessingWorker(
            AcademicLedgerProcessingStateService stateService,
            AcademicLedgerProcessingService processingService,
            AcademicLedgerValidationService validationService,
            AcademicLedgerProcessingProperties properties,
            Clock clock) {
        this.stateService = stateService;
        this.processingService = processingService;
        this.validationService = validationService;
        this.properties = properties;
        this.clock = clock;
    }

    @Scheduled(fixedDelayString = "${app.academics.ledger.processing.worker-poll-delay-ms:2000}")
    void poll() {
        try {
            recoverStaleWork();
            validationService.resumeOneReadyBatch();
            for (int processed = 0; processed < properties.maxUploadsPerPoll(); processed++) {
                var job = stateService.claimNextReceived();
                if (job.isEmpty()) {
                    break;
                }
                processingService.process(job.get());
            }
        } catch (RuntimeException exception) {
            // A scheduler invocation must not die permanently because one poll encountered an infrastructure error.
            LOGGER.error("Academic Ledger worker poll failed.", exception);
        }
    }

    private void recoverStaleWork() {
        OffsetDateTime staleBefore = OffsetDateTime.ofInstant(
                clock.instant().minus(properties.staleThreshold()), ZoneOffset.UTC);
        stateService.recoverOneStaleProcessing(staleBefore);
        validationService.recoverOneStaleValidation(staleBefore);
    }
}
