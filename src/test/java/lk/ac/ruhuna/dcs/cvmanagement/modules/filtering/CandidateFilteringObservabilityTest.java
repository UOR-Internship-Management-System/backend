package lk.ac.ruhuna.dcs.cvmanagement.modules.filtering;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import lk.ac.ruhuna.dcs.cvmanagement.infrastructure.observability.MetricsNames;
import lk.ac.ruhuna.dcs.cvmanagement.modules.filtering.application.CandidateFilteringDependencyExecutor;
import lk.ac.ruhuna.dcs.cvmanagement.modules.filtering.application.CandidateFilteringMetrics;
import lk.ac.ruhuna.dcs.cvmanagement.modules.filtering.domain.exception.FilterDependencyUnavailableException;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.dao.DataIntegrityViolationException;

class CandidateFilteringObservabilityTest {

    @Test
    void recordsLowCardinalityRunCountCandidateCountAndDatabaseTimers() {
        SimpleMeterRegistry registry = new SimpleMeterRegistry();
        CandidateFilteringMetrics metrics = new CandidateFilteringMetrics(registry);
        CandidateFilteringDependencyExecutor executor = new CandidateFilteringDependencyExecutor(metrics);

        assertThat(executor.execute("candidate_count", () -> 7L)).isEqualTo(7L);
        metrics.runCreated(7L);

        assertThat(registry.get(MetricsNames.CANDIDATE_FILTERING_RUN_CREATED).counter().count())
                .isEqualTo(1.0);
        assertThat(registry.get(MetricsNames.CANDIDATE_FILTERING_CANDIDATE_COUNT).summary().count())
                .isEqualTo(1L);
        assertThat(registry.get(MetricsNames.CANDIDATE_FILTERING_CANDIDATE_COUNT).summary().totalAmount())
                .isEqualTo(7.0);
        assertThat(registry.get(MetricsNames.CANDIDATE_FILTERING_DATABASE_DURATION)
                        .tag("operation", "candidate_count")
                        .tag("outcome", "success")
                        .timer()
                        .count())
                .isEqualTo(1L);
    }

    @Test
    void translatesOnlyTransientResourceFailuresAndKeepsIntegrityDefectsAsInternalFailures() {
        SimpleMeterRegistry registry = new SimpleMeterRegistry();
        CandidateFilteringMetrics metrics = new CandidateFilteringMetrics(registry);
        CandidateFilteringDependencyExecutor executor = new CandidateFilteringDependencyExecutor(metrics);

        DataAccessResourceFailureException transientFailure =
                new DataAccessResourceFailureException("offline");
        assertThatThrownBy(() -> executor.execute("candidate_page", () -> {
                    throw transientFailure;
                }))
                .isInstanceOf(FilterDependencyUnavailableException.class)
                .hasCause(transientFailure);

        DataIntegrityViolationException integrityFailure = new DataIntegrityViolationException("bad data");
        assertThatThrownBy(() -> executor.execute("persist_run", () -> {
                    throw integrityFailure;
                }))
                .isSameAs(integrityFailure);

        assertThat(registry.get(MetricsNames.CANDIDATE_FILTERING_DATABASE_FAILURES)
                        .tag("operation", "candidate_page")
                        .tag("failure_type", "dependency_unavailable")
                        .counter()
                        .count())
                .isEqualTo(1.0);
        assertThat(registry.get(MetricsNames.CANDIDATE_FILTERING_DATABASE_FAILURES)
                        .tag("operation", "persist_run")
                        .tag("failure_type", "unexpected_data_access")
                        .counter()
                        .count())
                .isEqualTo(1.0);
    }
}
