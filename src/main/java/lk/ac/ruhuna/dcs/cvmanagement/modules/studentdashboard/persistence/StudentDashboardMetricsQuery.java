package lk.ac.ruhuna.dcs.cvmanagement.modules.studentdashboard.persistence;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.UUID;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

/** Read-only counters scoped to a single student. */
@Repository
public class StudentDashboardMetricsQuery {

    private final JdbcTemplate jdbcTemplate;

    public StudentDashboardMetricsQuery(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public long countProjects(UUID studentId) {
        return count("SELECT COUNT(*) FROM student_projects WHERE student_id = ?", studentId);
    }

    public long countDeclaredSkills(UUID studentId) {
        return count("SELECT COUNT(*) FROM student_declared_skills WHERE student_id = ?", studentId);
    }

    /**
     * Counts distinct shortlists the student appears on.
     *
     * <p>Distinct by shortlist rather than by row: a student is only ever on a shortlist once
     * (enforced by {@code uq_shortlist_candidates_membership}), but counting shortlists is the
     * number the student actually cares about.
     */
    public long countShortlistMemberships(UUID studentId) {
        return count(
            "SELECT COUNT(DISTINCT shortlist_id) FROM shortlist_candidates WHERE student_id = ?",
            studentId);
    }

    /** Returns the committed cumulative GPA, or {@code null} when no ledger commit has run yet. */
    public BigDecimal findOfficialGpa(UUID studentId) {
        BigDecimal gpa = jdbcTemplate.query(
            """
            SELECT computer_science_gpa
            FROM academic.student_academic_summary
            WHERE student_id = ?
            """,
            resultSet -> resultSet.next() ? resultSet.getBigDecimal(1) : null,
            studentId);
        // The frontend contract requires a multiple of 0.01; the column is NUMERIC(3,2) but
        // normalising here keeps the API stable if that ever widens.
        return gpa == null ? null : gpa.setScale(2, RoundingMode.HALF_UP);
    }

    private long count(String sql, UUID studentId) {
        Long value = jdbcTemplate.queryForObject(sql, Long.class, studentId);
        return value == null ? 0L : value;
    }
}
