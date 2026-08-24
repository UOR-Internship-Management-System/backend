package lk.ac.ruhuna.dcs.cvmanagement.modules.shortlists.persistence.query;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.OffsetDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import lk.ac.ruhuna.dcs.cvmanagement.modules.shortlists.domain.policy.ShortlistStatus;
import lk.ac.ruhuna.dcs.cvmanagement.modules.shortlists.persistence.projection.ShortlistCandidateRow;
import lk.ac.ruhuna.dcs.cvmanagement.modules.shortlists.persistence.projection.ShortlistRequestContext;
import lk.ac.ruhuna.dcs.cvmanagement.modules.shortlists.persistence.projection.ShortlistSummaryRow;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

/** JDBC read model for bounded shortlist queries and cross-module reference facts. */
@Repository
public class ShortlistReadRepository {

    private static final String SUMMARY_SELECT = """
            SELECT s.id AS shortlist_id,
                   ir.id AS request_id,
                   c.id AS company_id,
                   c.name AS company_name,
                   ir.title AS role_title,
                   ir.shortlist_guidance_value AS request_guidance_value,
                   s.filter_run_id,
                   s.name,
                   s.status,
                   s.guidance_value_snapshot,
                   (SELECT COUNT(*) FROM shortlist_candidates sc WHERE sc.shortlist_id = s.id)
                       AS selected_candidate_count,
                   s.version,
                   s.created_at,
                   s.updated_at,
                   s.finalized_at
            FROM shortlists s
            JOIN internship_requests ir ON ir.id = s.internship_request_id
            JOIN companies c ON c.id = ir.company_id
            """;

    private final NamedParameterJdbcTemplate jdbc;

    public ShortlistReadRepository(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public Optional<ShortlistRequestContext> findRequest(UUID requestId) {
        String sql = """
                SELECT ir.id, ir.company_id, c.name AS company_name, ir.title,
                       ir.shortlist_guidance_value
                FROM internship_requests ir
                JOIN companies c ON c.id = ir.company_id
                WHERE ir.id = :requestId
                """;
        return jdbc.query(sql, Map.of("requestId", requestId), (rs, rowNum) -> new ShortlistRequestContext(
                        rs.getObject("id", UUID.class),
                        rs.getObject("company_id", UUID.class),
                        rs.getString("company_name"),
                        rs.getString("title"),
                        (Integer) rs.getObject("shortlist_guidance_value")))
                .stream()
                .findFirst();
    }

    public boolean filterRunMatchesRequest(UUID filterRunId, UUID requestId) {
        Long count = jdbc.queryForObject(
                "SELECT COUNT(*) FROM candidate_filter_runs "
                        + "WHERE id = :filterRunId AND internship_request_id = :requestId",
                Map.of("filterRunId", filterRunId, "requestId", requestId),
                Long.class);
        return count != null && count == 1;
    }

    public Set<UUID> findActiveStudentIds(Collection<UUID> studentIds) {
        if (studentIds == null || studentIds.isEmpty()) {
            return Set.of();
        }
        return Set.copyOf(jdbc.queryForList(
                "SELECT id FROM eligible_students WHERE is_active = TRUE AND id IN (:studentIds)",
                Map.of("studentIds", studentIds),
                UUID.class));
    }

    public Page<ShortlistSummaryRow> searchShortlists(
            String search,
            ShortlistStatus status,
            UUID companyId,
            int page,
            int size,
            String orderBy) {
        StringBuilder where = new StringBuilder("""
                 WHERE (:search = '' OR LOWER(COALESCE(s.name, '') || ' ' || c.name || ' ' || ir.title)
                        LIKE :searchPattern ESCAPE '\\')
                """);
        MapSqlParameterSource parameters = new MapSqlParameterSource()
                .addValue("search", search)
                .addValue("searchPattern", "%" + escapeLike(search.toLowerCase()) + "%")
                .addValue("limit", size)
                .addValue("offset", (long) page * size);
        if (status != null) {
            where.append(" AND s.status = :status");
            parameters.addValue("status", status.name());
        }
        if (companyId != null) {
            where.append(" AND c.id = :companyId");
            parameters.addValue("companyId", companyId);
        }

        List<ShortlistSummaryRow> rows = jdbc.query(
                SUMMARY_SELECT + where + " ORDER BY " + orderBy + " LIMIT :limit OFFSET :offset",
                parameters,
                this::mapSummary);
        Long total = jdbc.queryForObject(
                "SELECT COUNT(*) FROM shortlists s "
                        + "JOIN internship_requests ir ON ir.id = s.internship_request_id "
                        + "JOIN companies c ON c.id = ir.company_id " + where,
                parameters,
                Long.class);
        return new PageImpl<>(rows, PageRequest.of(page, size), total == null ? 0 : total);
    }

    public Optional<ShortlistSummaryRow> findSummary(UUID shortlistId) {
        return jdbc.query(
                        SUMMARY_SELECT + " WHERE s.id = :shortlistId",
                        Map.of("shortlistId", shortlistId),
                        this::mapSummary)
                .stream()
                .findFirst();
    }

    public Page<ShortlistCandidateRow> searchCandidates(
            UUID shortlistId,
            String search,
            int page,
            int size,
            String orderBy) {
        String fromWhere = """
                FROM shortlist_candidates sc
                JOIN eligible_students es ON es.id = sc.student_id
                LEFT JOIN academic.student_academic_summary sas ON sas.student_id = es.id
                WHERE sc.shortlist_id = :shortlistId
                  AND (:search = '' OR LOWER(es.full_name || ' ' || es.index_number)
                       LIKE :searchPattern ESCAPE '\\')
                """;
        MapSqlParameterSource parameters = new MapSqlParameterSource()
                .addValue("shortlistId", shortlistId)
                .addValue("search", search)
                .addValue("searchPattern", "%" + escapeLike(search.toLowerCase()) + "%")
                .addValue("limit", size)
                .addValue("offset", (long) page * size);
        String select = """
                SELECT es.id AS student_id,
                       es.index_number,
                       es.full_name,
                       sas.computer_science_gpa AS official_gpa,
                       EXISTS (
                           SELECT 1 FROM cvs cv
                           WHERE cv.student_id = es.id AND cv.pdf_file_asset_id IS NOT NULL
                       ) AS has_latest_saved_cv,
                       (
                           SELECT COUNT(*)
                           FROM shortlist_candidates other_sc
                           JOIN shortlists other_s ON other_s.id = other_sc.shortlist_id
                           WHERE other_sc.student_id = es.id
                             AND other_s.status IN ('DRAFT', 'FINALIZED')
                       ) AS active_shortlist_count,
                       sc.selected_at,
                       sc.selection_note
                """;
        List<ShortlistCandidateRow> rows = jdbc.query(
                select + fromWhere + " ORDER BY " + orderBy + " LIMIT :limit OFFSET :offset",
                parameters,
                this::mapCandidate);
        Long total = jdbc.queryForObject("SELECT COUNT(*) " + fromWhere, parameters, Long.class);
        return new PageImpl<>(rows, PageRequest.of(page, size), total == null ? 0 : total);
    }

    public Map<UUID, Integer> countActiveShortlists(Collection<UUID> studentIds) {
        if (studentIds == null || studentIds.isEmpty()) {
            return Map.of();
        }
        String sql = """
                SELECT sc.student_id, COUNT(*) AS shortlist_count
                FROM shortlist_candidates sc
                JOIN shortlists s ON s.id = sc.shortlist_id
                WHERE sc.student_id IN (:studentIds)
                  AND s.status IN ('DRAFT', 'FINALIZED')
                GROUP BY sc.student_id
                """;
        return jdbc.query(
                        sql,
                        Map.of("studentIds", studentIds),
                        (rs, rowNum) -> Map.entry(
                                rs.getObject("student_id", UUID.class),
                                rs.getInt("shortlist_count")))
                .stream()
                .collect(Collectors.toUnmodifiableMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    private ShortlistSummaryRow mapSummary(ResultSet rs, int rowNum) throws SQLException {
        return new ShortlistSummaryRow(
                rs.getObject("shortlist_id", UUID.class),
                rs.getObject("request_id", UUID.class),
                rs.getObject("company_id", UUID.class),
                rs.getString("company_name"),
                rs.getString("role_title"),
                (Integer) rs.getObject("request_guidance_value"),
                rs.getObject("filter_run_id", UUID.class),
                rs.getString("name"),
                ShortlistStatus.valueOf(rs.getString("status")),
                (Integer) rs.getObject("guidance_value_snapshot"),
                rs.getLong("selected_candidate_count"),
                rs.getLong("version"),
                rs.getObject("created_at", OffsetDateTime.class),
                rs.getObject("updated_at", OffsetDateTime.class),
                rs.getObject("finalized_at", OffsetDateTime.class));
    }

    private ShortlistCandidateRow mapCandidate(ResultSet rs, int rowNum) throws SQLException {
        return new ShortlistCandidateRow(
                rs.getObject("student_id", UUID.class),
                rs.getString("index_number"),
                rs.getString("full_name"),
                rs.getBigDecimal("official_gpa"),
                rs.getBoolean("has_latest_saved_cv"),
                rs.getInt("active_shortlist_count"),
                rs.getObject("selected_at", OffsetDateTime.class),
                rs.getString("selection_note"));
    }

    private String escapeLike(String value) {
        return value.replace("\\", "\\\\").replace("%", "\\%").replace("_", "\\_");
    }
}
