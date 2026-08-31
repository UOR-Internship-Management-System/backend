package lk.ac.ruhuna.dcs.cvmanagement.modules.studentdashboard.api.dto.response;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * Headline counters for the student's own dashboard.
 *
 * <p>{@code officialCumulativeGpa} is null until an academic ledger commit has produced a summary
 * row for the student. It is scaled to two decimals because the frontend contract requires a
 * multiple of 0.01.
 */
public record StudentDashboardMetricsResponse(
    long projectCount,
    long shortlistedInternshipCount,
    long declaredSkillCount,
    BigDecimal officialCumulativeGpa,
    Instant lastUpdatedAt) {
}
