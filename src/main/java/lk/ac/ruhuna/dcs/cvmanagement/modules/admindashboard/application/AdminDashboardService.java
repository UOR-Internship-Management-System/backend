package lk.ac.ruhuna.dcs.cvmanagement.modules.admindashboard.application;

import java.time.Clock;
import java.time.Instant;
import lk.ac.ruhuna.dcs.cvmanagement.modules.admindashboard.api.dto.response.AdminDashboardMetricsResponse;
import lk.ac.ruhuna.dcs.cvmanagement.modules.admindashboard.persistence.AdminDashboardMetricsQuery;
import lk.ac.ruhuna.dcs.cvmanagement.shared.error.DependencyUnavailableException;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AdminDashboardService {

    private final AdminDashboardMetricsQuery metricsQuery;
    private final Clock clock;

    public AdminDashboardService(AdminDashboardMetricsQuery metricsQuery, Clock clock) {
        this.metricsQuery = metricsQuery;
        this.clock = clock;
    }

    @Transactional(readOnly = true)
    public AdminDashboardMetricsResponse getMetrics() {
        try {
            return new AdminDashboardMetricsResponse(
                    metricsQuery.countTotalStudents(),
                    metricsQuery.countRegisteredStudents(),
                    metricsQuery.countInternshipRequests(),
                    Instant.now(clock));
        } catch (DataAccessException exception) {
            throw new DependencyUnavailableException(
                    "Admin dashboard metrics cannot be loaded at this time.");
        }
    }
}
