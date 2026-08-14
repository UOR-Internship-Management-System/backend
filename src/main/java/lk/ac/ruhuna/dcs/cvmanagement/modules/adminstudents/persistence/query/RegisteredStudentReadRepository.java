package lk.ac.ruhuna.dcs.cvmanagement.modules.adminstudents.persistence.query;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lk.ac.ruhuna.dcs.cvmanagement.modules.adminstudents.domain.model.RegisteredStudentSort;
import lk.ac.ruhuna.dcs.cvmanagement.modules.adminstudents.persistence.projection.RegisteredStudentRow;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

/**
 * Module-owned read adapter for the Admin registered-Student roster.
 *
 * <p>The registration predicate intentionally matches the Admin Dashboard definition: an active
 * eligible Student linked to an ACTIVE account that owns {@code ROLE_STUDENT}. Student-owned data
 * is never persisted or mutated by this repository.
 */
@Repository
public class RegisteredStudentReadRepository {

    private static final String RESOLVED_FULL_NAME =
            "COALESCE(NULLIF(BTRIM(sp.display_name), ''), es.full_name)";
    private static final String ACADEMIC_BATCH = "SPLIT_PART(es.index_number, '/', 2)";

    private static final String FROM_AND_JOINS = """
            FROM public.eligible_students es
            JOIN public.user_accounts ua ON ua.id = es.user_account_id
            JOIN public.user_roles ur ON ur.user_id = ua.id
            JOIN public.roles r ON r.id = ur.role_id
            LEFT JOIN public.student_profiles sp ON sp.student_id = es.id
            LEFT JOIN academic.student_academic_summary aas ON aas.student_id = es.id
            """;

    private static final String REGISTERED_STUDENT_PREDICATE = """
            es.is_active = TRUE
            AND ua.account_status = 'ACTIVE'
            AND r.name = 'ROLE_STUDENT'
            """;

    private final NamedParameterJdbcTemplate jdbcTemplate;

    public RegisteredStudentReadRepository(NamedParameterJdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }




    /**
     * Checks the registration/access predicate without joining optional profile or academic data.
     *
     * <p>Child inspection resources use this lightweight guard so skills/projects availability does
     * not become coupled to the Academic Ledger schema being queryable.
     */
    public boolean existsRegisteredStudent(UUID studentId) {
        MapSqlParameterSource parameters = new MapSqlParameterSource("studentId", studentId);
        Boolean exists = jdbcTemplate.queryForObject(
                """
                SELECT EXISTS (
                    SELECT 1
                    FROM public.eligible_students es
                    JOIN public.user_accounts ua ON ua.id = es.user_account_id
                    JOIN public.user_roles ur ON ur.user_id = ua.id
                    JOIN public.roles r ON r.id = ur.role_id
                    WHERE es.id = :studentId
                      AND es.is_active = TRUE
                      AND ua.account_status = 'ACTIVE'
                      AND r.name = 'ROLE_STUDENT'
                )
                """,
                parameters,
                Boolean.class);
        return Boolean.TRUE.equals(exists);
    }

    /** Returns one registered Student using the exact same predicate as the roster. */
    public Optional<RegisteredStudentRow> findById(UUID studentId) {
        MapSqlParameterSource parameters = new MapSqlParameterSource("studentId", studentId);
        String sql = """
                SELECT es.id AS student_id,
                       es.index_number,
                       %s AS resolved_full_name,
                       es.university_email,
                       %s AS academic_batch,
                       es.academic_level AS current_level,
                       aas.computer_science_gpa AS official_gpa
                """.formatted(RESOLVED_FULL_NAME, ACADEMIC_BATCH)
                + FROM_AND_JOINS
                + " WHERE es.id = :studentId AND " + REGISTERED_STUDENT_PREDICATE;

        return jdbcTemplate.query(sql, parameters, this::mapRow).stream().findFirst();
    }

    public Page<RegisteredStudentRow> search(
            String normalizedSearch,
            Integer level,
            int page,
            int size,
            RegisteredStudentSort sort) {
        MapSqlParameterSource parameters = new MapSqlParameterSource();
        List<String> predicates = new ArrayList<>();
        predicates.add(REGISTERED_STUDENT_PREDICATE);

        if (normalizedSearch != null) {
            predicates.add("(" +
                    "LOWER(" + RESOLVED_FULL_NAME + ") LIKE :searchPattern ESCAPE '\\' " +
                    "OR LOWER(es.index_number) LIKE :searchPattern ESCAPE '\\' " +
                    "OR LOWER(es.university_email) LIKE :searchPattern ESCAPE '\\' " +
                    "OR " + ACADEMIC_BATCH + " LIKE :searchPattern ESCAPE '\\'" +
                    ")");
            parameters.addValue("searchPattern", likePattern(normalizedSearch));
        }

        if (level != null) {
            predicates.add("es.academic_level = :level");
            parameters.addValue("level", level);
        }

        String where = " WHERE " + String.join(" AND ", predicates);
        Long total = jdbcTemplate.queryForObject(
                "SELECT COUNT(DISTINCT es.id) " + FROM_AND_JOINS + where,
                parameters,
                Long.class);
        long totalElements = total == null ? 0L : total;

        parameters.addValue("limit", size);
        parameters.addValue("offset", Math.multiplyExact((long) page, (long) size));

        String dataSql = """
                SELECT es.id AS student_id,
                       es.index_number,
                       %s AS resolved_full_name,
                       es.university_email,
                       %s AS academic_batch,
                       es.academic_level AS current_level,
                       aas.computer_science_gpa AS official_gpa
                """.formatted(RESOLVED_FULL_NAME, ACADEMIC_BATCH)
                + FROM_AND_JOINS
                + where
                + " ORDER BY " + sort.sqlOrder()
                + " LIMIT :limit OFFSET :offset";

        List<RegisteredStudentRow> items = jdbcTemplate.query(dataSql, parameters, this::mapRow);
        return new PageImpl<>(items, PageRequest.of(page, size), totalElements);
    }

    private RegisteredStudentRow mapRow(ResultSet resultSet, int rowNumber) throws SQLException {
        return new RegisteredStudentRow(
                resultSet.getObject("student_id", java.util.UUID.class),
                resultSet.getString("index_number"),
                resultSet.getString("resolved_full_name"),
                resultSet.getString("university_email"),
                resultSet.getString("academic_batch"),
                resultSet.getInt("current_level"),
                resultSet.getBigDecimal("official_gpa"));
    }

    private String likePattern(String value) {
        String escaped = value.replace("\\", "\\\\").replace("%", "\\%").replace("_", "\\_");
        return "%" + escaped + "%";
    }
}
