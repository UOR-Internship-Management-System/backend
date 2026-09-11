package lk.ac.ruhuna.dcs.cvmanagement.modules.filtering.application;

import io.micrometer.core.instrument.Timer;
import java.util.Objects;
import java.util.function.Supplier;
import lk.ac.ruhuna.dcs.cvmanagement.modules.filtering.domain.exception.FilterDependencyUnavailableException;
import lk.ac.ruhuna.dcs.cvmanagement.shared.http.CorrelationIdContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.dao.TransientDataAccessException;
import org.springframework.stereotype.Component;

/**
 * Executes Candidate Filtering database work with consistent timing, safe failure logging, and
 * public dependency-failure translation.
 *
 * <p>Only transient/resource failures become {@code FILTER_DEPENDENCY_UNAVAILABLE}. Programming,
 * SQL-shape, mapping, and integrity defects are intentionally rethrown so they remain HTTP 500 and
 * are not mislabeled as temporary dependency outages.
 */
@Component
public class CandidateFilteringDependencyExecutor {

    private static final Logger LOGGER = LoggerFactory.getLogger(CandidateFilteringDependencyExecutor.class);

    private final CandidateFilteringMetrics metrics;

    public CandidateFilteringDependencyExecutor(CandidateFilteringMetrics metrics) {
        this.metrics = metrics;
    }

    public <T> T execute(String operation, Supplier<T> action) {
        Objects.requireNonNull(operation, "operation is required.");
        Objects.requireNonNull(action, "action is required.");

        Timer.Sample sample = metrics.startDatabaseOperation();
        try {
            T result = action.get();
            metrics.databaseOperationSucceeded(operation, sample);
            return result;
        } catch (DataAccessResourceFailureException | TransientDataAccessException exception) {
            metrics.databaseOperationFailed(operation, "dependency_unavailable", sample);
            LOGGER.warn(
                    "Candidate Filtering database dependency unavailable operation={} exceptionType={} correlationId={}",
                    operation,
                    exception.getClass().getSimpleName(),
                    correlationId());
            throw new FilterDependencyUnavailableException(exception);
        } catch (DataAccessException exception) {
            metrics.databaseOperationFailed(operation, "unexpected_data_access", sample);
            LOGGER.error(
                    "Candidate Filtering database operation failed operation={} exceptionType={} correlationId={}",
                    operation,
                    exception.getClass().getSimpleName(),
                    correlationId());
            throw exception;
        } catch (RuntimeException exception) {
            metrics.databaseOperationCompleted(operation, "application_error", sample);
            throw exception;
        }
    }

    private String correlationId() {
        return CorrelationIdContext.current().orElse("none");
    }
}
