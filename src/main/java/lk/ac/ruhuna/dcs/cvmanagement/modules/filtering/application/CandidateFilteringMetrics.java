package lk.ac.ruhuna.dcs.cvmanagement.modules.filtering.application;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.DistributionSummary;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import lk.ac.ruhuna.dcs.cvmanagement.infrastructure.observability.MetricsNames;
import org.springframework.stereotype.Component;

/** Low-cardinality Micrometer instrumentation for Candidate Filtering operations. */
@Component
public class CandidateFilteringMetrics {

    private final MeterRegistry meterRegistry;
    private final Counter runsCreated;
    private final DistributionSummary candidateCount;

    public CandidateFilteringMetrics(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
        this.runsCreated = Counter.builder(MetricsNames.CANDIDATE_FILTERING_RUN_CREATED)
                .description("Successfully created deterministic Candidate Filtering runs")
                .register(meterRegistry);
        this.candidateCount = DistributionSummary.builder(MetricsNames.CANDIDATE_FILTERING_CANDIDATE_COUNT)
                .description("Current candidate counts observed for deterministic filtering runs")
                .baseUnit("candidates")
                .register(meterRegistry);
    }

    public Timer.Sample startDatabaseOperation() {
        return Timer.start(meterRegistry);
    }

    public void databaseOperationSucceeded(String operation, Timer.Sample sample) {
        databaseOperationCompleted(operation, "success", sample);
    }

    public void databaseOperationCompleted(String operation, String outcome, Timer.Sample sample) {
        stopDatabaseTimer(operation, outcome, sample);
    }

    public void databaseOperationFailed(String operation, String failureType, Timer.Sample sample) {
        Counter.builder(MetricsNames.CANDIDATE_FILTERING_DATABASE_FAILURES)
                .description("Candidate Filtering database operation failures")
                .tag("operation", operation)
                .tag("failure_type", failureType)
                .register(meterRegistry)
                .increment();
        stopDatabaseTimer(operation, "failure", sample);
    }

    public void runCreated(long currentCandidateCount) {
        runsCreated.increment();
        recordCandidateCount(currentCandidateCount);
    }

    public void recordCandidateCount(long currentCandidateCount) {
        if (currentCandidateCount >= 0) {
            candidateCount.record(currentCandidateCount);
        }
    }

    private void stopDatabaseTimer(String operation, String outcome, Timer.Sample sample) {
        sample.stop(Timer.builder(MetricsNames.CANDIDATE_FILTERING_DATABASE_DURATION)
                .description("Candidate Filtering database operation duration")
                .tag("operation", operation)
                .tag("outcome", outcome)
                .register(meterRegistry));
    }
}
