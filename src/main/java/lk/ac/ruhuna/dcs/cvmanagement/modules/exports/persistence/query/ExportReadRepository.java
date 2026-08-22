package lk.ac.ruhuna.dcs.cvmanagement.modules.exports.persistence.query;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

/** Bounded, export-owned read model over finalized shortlist data. */
@Repository
public class ExportReadRepository {
    private final JdbcClient jdbc;

    public ExportReadRepository(JdbcClient jdbc) { this.jdbc = jdbc; }

    public Optional<ExportShortlist> findShortlist(UUID shortlistId) {
        return jdbc.sql("""
                SELECT s.id, (s.status = 'FINALIZED') AS finalized,
                       s.name, c.name AS company_name, ir.title AS role_title
                FROM shortlists s
                JOIN internship_requests ir ON ir.id = s.internship_request_id
                JOIN companies c ON c.id = ir.company_id
                WHERE s.id = :shortlistId
                """)
                .param("shortlistId", shortlistId)
                .query((rs, row) -> new ExportShortlist(
                        rs.getObject("id", UUID.class),
                        rs.getBoolean("finalized"),
                        rs.getString("name"),
                        rs.getString("company_name"),
                        rs.getString("role_title")))
                .optional();
    }

    public List<ExportCandidate> findCandidates(UUID shortlistId) {
        return jdbc.sql("""
                SELECT es.id AS student_id, es.index_number, es.full_name,
                       sas.computer_science_gpa, sc.selected_at
                FROM shortlist_candidates sc
                JOIN eligible_students es ON es.id = sc.student_id
                LEFT JOIN academic.student_academic_summary sas ON sas.student_id = es.id
                WHERE sc.shortlist_id = :shortlistId
                ORDER BY es.index_number ASC, es.id ASC
                """)
                .param("shortlistId", shortlistId)
                .query((rs, row) -> new ExportCandidate(
                        rs.getObject("student_id", UUID.class),
                        rs.getString("index_number"),
                        rs.getString("full_name"),
                        rs.getObject("computer_science_gpa", BigDecimal.class),
                        rs.getObject("selected_at", OffsetDateTime.class)))
                .list();
    }

    public record ExportShortlist(
            UUID shortlistId, boolean finalized, String name, String companyName, String roleTitle) {}

    public record ExportCandidate(
            UUID studentId, String indexNumber, String fullName, BigDecimal officialGpa, OffsetDateTime selectedAt) {}
}
