package lk.ac.ruhuna.dcs.cvmanagement.modules.filtering.persistence.query;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import lk.ac.ruhuna.dcs.cvmanagement.modules.filtering.domain.policy.CandidateDeclaredSkillCompetencyLevel;
import lk.ac.ruhuna.dcs.cvmanagement.modules.filtering.domain.policy.CandidateFilteringCriteria;
import lk.ac.ruhuna.dcs.cvmanagement.modules.filtering.domain.policy.CandidateSort;
import lk.ac.ruhuna.dcs.cvmanagement.modules.filtering.domain.policy.FilterSkillMatchMode;
import lk.ac.ruhuna.dcs.cvmanagement.modules.filtering.persistence.projection.CandidateFilterRow;
import lk.ac.ruhuna.dcs.cvmanagement.modules.filtering.persistence.projection.CandidateMatchingSkillRow;
import lk.ac.ruhuna.dcs.cvmanagement.modules.filtering.persistence.projection.FilterRequestSummaryRow;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

/**
 * Filtering-owned JDBC read adapter for deterministic Candidate Filtering.
 *
 * <p>The module intentionally reads the authoritative tables directly instead of importing Java
 * services or repositories from Academics, Skills, Internship Requests, or Admin Student
 * Inspection. This preserves the modular-monolith dependency rules while keeping eligibility
 * evaluation set-based inside PostgreSQL.
 */
@Repository
public class CandidateFilteringReadRepository {

    private static final int MAX_PAGE_SIZE = 100;

    private static final String RESOLVED_FULL_NAME =
            "COALESCE(NULLIF(BTRIM(sp.display_name), ''), es.full_name)";

    private static final String CANDIDATE_FROM_AND_JOINS = """
            FROM public.eligible_students es
            JOIN public.user_accounts ua ON ua.id = es.user_account_id
            JOIN public.user_roles ur ON ur.user_id = ua.id
            JOIN public.roles r ON r.id = ur.role_id AND r.name = 'ROLE_STUDENT'
            LEFT JOIN public.student_profiles sp ON sp.student_id = es.id
            LEFT JOIN academic.student_academic_summary aas ON aas.student_id = es.id
            """;

    private static final String BASE_ELIGIBILITY_PREDICATE = """
            es.is_active = TRUE
            AND ua.account_status = 'ACTIVE'
            """;

    private final NamedParameterJdbcTemplate jdbcTemplate;

    public CandidateFilteringReadRepository(NamedParameterJdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    /** Returns the current request/company context without importing the Internship module. */
    public Optional<FilterRequestSummaryRow> findRequestSummary(UUID requestId) {
        MapSqlParameterSource parameters = new MapSqlParameterSource("requestId", requestId);
        return jdbcTemplate.query(
                        """
                        SELECT ir.id AS request_id,
                               c.id AS company_id,
                               c.name AS company_name,
                               ir.title,
                               ir.shortlist_guidance_value
                        FROM public.internship_requests ir
                        JOIN public.companies c ON c.id = ir.company_id
                        WHERE ir.id = :requestId
                        """,
                        parameters,
                        this::mapRequestSummary)
                .stream()
                .findFirst();
    }

    /**
     * Returns the supplied skill IDs that are currently required by the selected internship
     * request. The caller can compare this set with submitted request-skill criteria and can also
     * use it to reject additional skills that duplicate any request-required skill.
     */
    public Set<UUID> findRequiredSkillIds(UUID requestId, Collection<UUID> skillIds) {
        if (skillIds == null || skillIds.isEmpty()) {
            return Set.of();
        }

        MapSqlParameterSource parameters = new MapSqlParameterSource()
                .addValue("requestId", requestId)
                .addValue("skillIds", List.copyOf(skillIds));

        List<UUID> rows = jdbcTemplate.queryForList(
                """
                SELECT irs.skill_id
                FROM public.internship_request_skills irs
                WHERE irs.internship_request_id = :requestId
                  AND irs.skill_id IN (:skillIds)
                ORDER BY irs.skill_id
                """,
                parameters,
                UUID.class);
        return Set.copyOf(rows);
    }

    /** Returns the supplied IDs that currently refer to ACTIVE canonical taxonomy skills. */
    public Set<UUID> findActiveSkillIds(Collection<UUID> skillIds) {
        if (skillIds == null || skillIds.isEmpty()) {
            return Set.of();
        }

        MapSqlParameterSource parameters =
                new MapSqlParameterSource("skillIds", List.copyOf(skillIds));
        List<UUID> rows = jdbcTemplate.queryForList(
                """
                SELECT s.id
                FROM public.skills s
                WHERE s.id IN (:skillIds)
                  AND s.skill_status = 'ACTIVE'
                ORDER BY s.id
                """,
                parameters,
                UUID.class);
        return Set.copyOf(rows);
    }

    /** Counts current candidates for the persisted runtime criteria, without UI search narrowing. */
    public long countCandidates(CandidateFilteringCriteria criteria) {
        QueryParts query = buildCandidateQuery(criteria, null);
        Long total = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) " + CANDIDATE_FROM_AND_JOINS + query.whereClause(),
                query.parameters(),
                Long.class);
        return total == null ? 0L : total;
    }

    /**
     * Returns a deterministic, server-side candidate page from current committed data.
     *
     * <p>Candidate result rows are never read from a snapshot table. GPA and declared-skill changes
     * therefore become visible when the same run is queried again, as required by the current API
     * contract.
     */
    public Page<CandidateFilterRow> searchCandidates(
            CandidateFilteringCriteria criteria,
            String search,
            int page,
            int size,
            CandidateSort sort) {
        requirePageBounds(page, size);
        CandidateSort safeSort = sort == null ? CandidateSort.DEFAULT : sort;
        String normalizedSearch = normalizeSearch(search);
        QueryParts query = buildCandidateQuery(criteria, normalizedSearch);

        Long total = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) " + CANDIDATE_FROM_AND_JOINS + query.whereClause(),
                query.parameters(),
                Long.class);
        long totalElements = total == null ? 0L : total;

        MapSqlParameterSource pageParameters = copyParameters(query.parameters())
                .addValue("limit", size)
                .addValue("offset", Math.multiplyExact((long) page, (long) size));

        String dataSql = """
                SELECT es.id AS student_id,
                       es.index_number,
                       %s AS resolved_full_name,
                       aas.computer_science_gpa AS official_gpa,
                       (
                           SELECT COUNT(*)
                           FROM public.student_declared_skills all_ds
                           WHERE all_ds.student_id = es.id
                       ) AS declared_skill_count
                """.formatted(RESOLVED_FULL_NAME)
                + CANDIDATE_FROM_AND_JOINS
                + query.whereClause()
                + " ORDER BY " + orderBy(safeSort)
                + " LIMIT :limit OFFSET :offset";

        List<CandidateFilterRow> items =
                jdbcTemplate.query(dataSql, pageParameters, this::mapCandidateRow);
        return new PageImpl<>(items, PageRequest.of(page, size), totalElements);
    }

    /**
     * Bulk-loads only the selected criteria skills declared by candidates on the current page.
     * Returns no rows when either input is empty and never performs one query per Student.
     */
    public List<CandidateMatchingSkillRow> findMatchingDeclaredSkills(
            Collection<UUID> studentIds,
            Collection<UUID> selectedSkillIds) {
        if (studentIds == null
                || studentIds.isEmpty()
                || selectedSkillIds == null
                || selectedSkillIds.isEmpty()) {
            return List.of();
        }

        MapSqlParameterSource parameters = new MapSqlParameterSource()
                .addValue("studentIds", List.copyOf(studentIds))
                .addValue("selectedSkillIds", List.copyOf(selectedSkillIds));

        return jdbcTemplate.query(
                """
                SELECT ds.student_id,
                       ds.id AS declared_skill_id,
                       ds.skill_id,
                       s.skill_name,
                       ds.competency_level,
                       ds.version,
                       ds.created_at,
                       ds.updated_at
                FROM public.student_declared_skills ds
                JOIN public.skills s ON s.id = ds.skill_id
                WHERE ds.student_id IN (:studentIds)
                  AND ds.skill_id IN (:selectedSkillIds)
                ORDER BY ds.student_id,
                         LOWER(s.skill_name),
                         s.skill_name,
                         ds.skill_id
                """,
                parameters,
                this::mapMatchingSkillRow);
    }

    private QueryParts buildCandidateQuery(
            CandidateFilteringCriteria criteria,
            String normalizedSearch) {
        if (criteria == null) {
            throw new IllegalArgumentException("criteria is required.");
        }

        MapSqlParameterSource parameters = new MapSqlParameterSource();
        List<String> predicates = new ArrayList<>();
        predicates.add(BASE_ELIGIBILITY_PREDICATE);

        BigDecimal lowerBound = criteria.runtimeGpaLowerBound();
        if (lowerBound != null) {
            predicates.add("aas.computer_science_gpa >= :runtimeGpaLowerBound");
            parameters.addValue("runtimeGpaLowerBound", lowerBound);
        }

        BigDecimal upperBound = criteria.runtimeGpaUpperBound();
        if (upperBound != null) {
            predicates.add("aas.computer_science_gpa <= :runtimeGpaUpperBound");
            parameters.addValue("runtimeGpaUpperBound", upperBound);
        }

        if (criteria.hasSkillCriteria()) {
            parameters.addValue("selectedSkillIds", criteria.selectedSkillIds());
            if (criteria.skillMatchMode() == FilterSkillMatchMode.AND) {
                predicates.add("""
                        es.id IN (
                            SELECT matched_ds.student_id
                            FROM public.student_declared_skills matched_ds
                            WHERE matched_ds.skill_id IN (:selectedSkillIds)
                            GROUP BY matched_ds.student_id
                            HAVING COUNT(DISTINCT matched_ds.skill_id) = :selectedSkillCount
                        )
                        """);
                parameters.addValue("selectedSkillCount", criteria.selectedSkillCount());
            } else {
                predicates.add("""
                        es.id IN (
                            SELECT matched_ds.student_id
                            FROM public.student_declared_skills matched_ds
                            WHERE matched_ds.skill_id IN (:selectedSkillIds)
                        )
                        """);
            }
        }

        if (normalizedSearch != null) {
            predicates.add("(" +
                    "LOWER(" + RESOLVED_FULL_NAME + ") LIKE :searchPattern ESCAPE '\\' " +
                    "OR LOWER(es.index_number) LIKE :searchPattern ESCAPE '\\'" +
                    ")");
            parameters.addValue("searchPattern", likePattern(normalizedSearch));
        }

        return new QueryParts(" WHERE " + String.join(" AND ", predicates), parameters);
    }

    private FilterRequestSummaryRow mapRequestSummary(ResultSet resultSet, int rowNumber)
            throws SQLException {
        return new FilterRequestSummaryRow(
                resultSet.getObject("request_id", UUID.class),
                resultSet.getObject("company_id", UUID.class),
                resultSet.getString("company_name"),
                resultSet.getString("title"),
                resultSet.getObject("shortlist_guidance_value", Integer.class));
    }

    private CandidateFilterRow mapCandidateRow(ResultSet resultSet, int rowNumber)
            throws SQLException {
        return new CandidateFilterRow(
                resultSet.getObject("student_id", UUID.class),
                resultSet.getString("index_number"),
                resultSet.getString("resolved_full_name"),
                resultSet.getBigDecimal("official_gpa"),
                Math.toIntExact(resultSet.getLong("declared_skill_count")));
    }

    private CandidateMatchingSkillRow mapMatchingSkillRow(ResultSet resultSet, int rowNumber)
            throws SQLException {
        return new CandidateMatchingSkillRow(
                resultSet.getObject("student_id", UUID.class),
                resultSet.getObject("declared_skill_id", UUID.class),
                resultSet.getObject("skill_id", UUID.class),
                resultSet.getString("skill_name"),
                CandidateDeclaredSkillCompetencyLevel.valueOf(resultSet.getString("competency_level")),
                resultSet.getLong("version"),
                resultSet.getObject("created_at", java.time.OffsetDateTime.class),
                resultSet.getObject("updated_at", java.time.OffsetDateTime.class));
    }

    private String orderBy(CandidateSort sort) {
        return switch (sort) {
            case OFFICIAL_GPA_DESC -> "aas.computer_science_gpa DESC NULLS LAST, es.id ASC";
            case OFFICIAL_GPA_ASC -> "aas.computer_science_gpa ASC NULLS LAST, es.id ASC";
            case FULL_NAME_ASC -> "LOWER(" + RESOLVED_FULL_NAME + ") ASC, es.id ASC";
            case INDEX_NUMBER_ASC -> "LOWER(es.index_number) ASC, es.id ASC";
        };
    }

    private void requirePageBounds(int page, int size) {
        if (page < 0) {
            throw new IllegalArgumentException("page must be greater than or equal to 0.");
        }
        if (size < 1 || size > MAX_PAGE_SIZE) {
            throw new IllegalArgumentException("size must be between 1 and 100.");
        }
    }

    private String normalizeSearch(String value) {
        if (value == null) {
            return null;
        }
        String normalized = value.strip().toLowerCase(Locale.ROOT);
        return normalized.isEmpty() ? null : normalized;
    }

    private String likePattern(String normalizedValue) {
        String escaped = normalizedValue
                .replace("\\", "\\\\")
                .replace("%", "\\%")
                .replace("_", "\\_");
        return "%" + escaped + "%";
    }

    private MapSqlParameterSource copyParameters(MapSqlParameterSource source) {
        MapSqlParameterSource copy = new MapSqlParameterSource();
        for (String parameterName : source.getParameterNames()) {
            copy.addValue(parameterName, source.getValue(parameterName));
        }
        return copy;
    }

    private record QueryParts(String whereClause, MapSqlParameterSource parameters) {
    }
}
