package lk.ac.ruhuna.dcs.cvmanagement.modules.admindashboard.api.dto.response;

import java.time.Instant;

public record AdminDashboardMetricsResponse(
        long totalStudents,
        long registeredStudents,
        long internshipRequestsCreated,
        Instant lastUpdatedAt) {
}
