package lk.ac.ruhuna.dcs.cvmanagement.modules.adminstudents.persistence.query;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lk.ac.ruhuna.dcs.cvmanagement.modules.adminstudents.persistence.projection.AdminActivityRow;
import lk.ac.ruhuna.dcs.cvmanagement.modules.adminstudents.persistence.projection.AdminAwardRow;
import lk.ac.ruhuna.dcs.cvmanagement.modules.adminstudents.persistence.projection.AdminCertificateRow;
import lk.ac.ruhuna.dcs.cvmanagement.modules.adminstudents.persistence.projection.AdminExperienceRow;
import lk.ac.ruhuna.dcs.cvmanagement.modules.adminstudents.persistence.projection.AdminStudentProfileRow;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

/**
 * Module-owned read adapter for the Admin Student deep-dive.
 *
 * <p>This repository intentionally reads the authoritative Student-owned tables directly rather
 * than importing another module's repository/service implementation. Every statement is a SELECT;
 * Admin inspection cannot create or update Student-owned rows through this adapter.
 */
@Repository
public class AdminStudentDetailReadRepository {

    private static final String REGISTERED_STUDENT_PREDICATE = """
            es.is_active = TRUE
            AND ua.account_status = 'ACTIVE'
            AND r.name = 'ROLE_STUDENT'
            """;

    private final NamedParameterJdbcTemplate jdbcTemplate;

    public AdminStudentDetailReadRepository(NamedParameterJdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    /**
     * Loads the read-only profile projection for one registered Student.
     *
     * <p>The profile row itself is optional. Identity fields and timestamps fall back to
     * {@code eligible_students}, so a registered Student remains inspectable without causing the
     * Student profile module's lazy profile-creation path to run.
     */
    public Optional<AdminStudentProfileRow> findProfile(UUID studentId) {
        String sql = """
                SELECT es.id AS student_id,
                       COALESCE(NULLIF(BTRIM(sp.display_name), ''), es.full_name) AS resolved_full_name,
                       es.index_number,
                       es.university_email,
                       es.academic_level AS student_level,
                       CAST(NULLIF(SPLIT_PART(es.index_number, '/', 2), '') AS INTEGER) AS cohort_year,
                       sp.personal_email,
                       sp.headline,
                       sp.summary,
                       sp.phone,
                       sp.location,
                       COALESCE(sp.version, 0) AS profile_version,
                       COALESCE(sp.updated_at, es.updated_at) AS profile_updated_at,
                       GREATEST(
                           es.updated_at,
                           COALESCE(sp.updated_at, es.updated_at),
                           COALESCE(contact_links.latest_updated_at, es.updated_at),
                           COALESCE(certificates.latest_updated_at, es.updated_at),
                           COALESCE(awards.latest_updated_at, es.updated_at),
                           COALESCE(activities.latest_updated_at, es.updated_at),
                           COALESCE(experiences.latest_updated_at, es.updated_at)
                       ) AS cv_source_updated_at
                FROM public.eligible_students es
                JOIN public.user_accounts ua ON ua.id = es.user_account_id
                JOIN public.user_roles ur ON ur.user_id = ua.id
                JOIN public.roles r ON r.id = ur.role_id
                LEFT JOIN public.student_profiles sp ON sp.student_id = es.id
                LEFT JOIN LATERAL (
                    SELECT MAX(updated_at) AS latest_updated_at
                    FROM public.student_contact_links
                    WHERE student_id = es.id
                ) contact_links ON TRUE
                LEFT JOIN LATERAL (
                    SELECT MAX(updated_at) AS latest_updated_at
                    FROM public.student_certificates
                    WHERE student_id = es.id
                ) certificates ON TRUE
                LEFT JOIN LATERAL (
                    SELECT MAX(updated_at) AS latest_updated_at
                    FROM public.student_awards
                    WHERE student_id = es.id
                ) awards ON TRUE
                LEFT JOIN LATERAL (
                    SELECT MAX(updated_at) AS latest_updated_at
                    FROM public.student_activities
                    WHERE student_id = es.id
                ) activities ON TRUE
                LEFT JOIN LATERAL (
                    SELECT MAX(updated_at) AS latest_updated_at
                    FROM public.student_work_experience
                    WHERE student_id = es.id
                ) experiences ON TRUE
                WHERE es.id = :studentId
                  AND %s
                """.formatted(REGISTERED_STUDENT_PREDICATE);

        return jdbcTemplate.query(
                        sql,
                        new MapSqlParameterSource("studentId", studentId),
                        this::mapProfile)
                .stream()
                .findFirst();
    }

    public List<AdminExperienceRow> findExperiences(UUID studentId) {
        String sql = """
                SELECT id,
                       organization,
                       position_title,
                       location,
                       start_date,
                       end_date,
                       is_current_role,
                       description,
                       cv_include,
                       version,
                       created_at,
                       updated_at
                FROM public.student_work_experience
                WHERE student_id = :studentId
                ORDER BY updated_at DESC, id ASC
                """;
        return jdbcTemplate.query(sql, parameters(studentId), this::mapExperience);
    }

    public List<AdminCertificateRow> findCertificates(UUID studentId) {
        String sql = """
                SELECT id,
                       title,
                       issuer,
                       issue_date,
                       credential_url,
                       cv_include,
                       version,
                       created_at,
                       updated_at
                FROM public.student_certificates
                WHERE student_id = :studentId
                ORDER BY updated_at DESC, id ASC
                """;
        return jdbcTemplate.query(sql, parameters(studentId), this::mapCertificate);
    }

    public List<AdminAwardRow> findAwards(UUID studentId) {
        String sql = """
                SELECT id,
                       title,
                       issuer,
                       award_date,
                       description,
                       cv_include,
                       version,
                       created_at,
                       updated_at
                FROM public.student_awards
                WHERE student_id = :studentId
                ORDER BY updated_at DESC, id ASC
                """;
        return jdbcTemplate.query(sql, parameters(studentId), this::mapAward);
    }

    public List<AdminActivityRow> findActivities(UUID studentId) {
        String sql = """
                SELECT id,
                       activity_name,
                       role_title,
                       start_date,
                       end_date,
                       description,
                       cv_include,
                       version,
                       created_at,
                       updated_at
                FROM public.student_activities
                WHERE student_id = :studentId
                ORDER BY updated_at DESC, id ASC
                """;
        return jdbcTemplate.query(sql, parameters(studentId), this::mapActivity);
    }

    private MapSqlParameterSource parameters(UUID studentId) {
        return new MapSqlParameterSource("studentId", studentId);
    }

    private AdminStudentProfileRow mapProfile(ResultSet resultSet, int rowNumber) throws SQLException {
        return new AdminStudentProfileRow(
                resultSet.getObject("student_id", UUID.class),
                resultSet.getString("resolved_full_name"),
                resultSet.getString("index_number"),
                resultSet.getString("university_email"),
                resultSet.getInt("student_level"),
                resultSet.getObject("cohort_year", Integer.class),
                resultSet.getString("personal_email"),
                resultSet.getString("headline"),
                resultSet.getString("summary"),
                resultSet.getString("phone"),
                resultSet.getString("location"),
                resultSet.getLong("profile_version"),
                resultSet.getObject("profile_updated_at", OffsetDateTime.class),
                resultSet.getObject("cv_source_updated_at", OffsetDateTime.class));
    }

    private AdminExperienceRow mapExperience(ResultSet resultSet, int rowNumber) throws SQLException {
        return new AdminExperienceRow(
                resultSet.getObject("id", UUID.class),
                resultSet.getString("organization"),
                resultSet.getString("position_title"),
                resultSet.getString("location"),
                resultSet.getObject("start_date", LocalDate.class),
                resultSet.getObject("end_date", LocalDate.class),
                resultSet.getBoolean("is_current_role"),
                resultSet.getString("description"),
                resultSet.getBoolean("cv_include"),
                resultSet.getLong("version"),
                resultSet.getObject("created_at", OffsetDateTime.class),
                resultSet.getObject("updated_at", OffsetDateTime.class));
    }

    private AdminCertificateRow mapCertificate(ResultSet resultSet, int rowNumber) throws SQLException {
        return new AdminCertificateRow(
                resultSet.getObject("id", UUID.class),
                resultSet.getString("title"),
                resultSet.getString("issuer"),
                resultSet.getObject("issue_date", LocalDate.class),
                resultSet.getString("credential_url"),
                resultSet.getBoolean("cv_include"),
                resultSet.getLong("version"),
                resultSet.getObject("created_at", OffsetDateTime.class),
                resultSet.getObject("updated_at", OffsetDateTime.class));
    }

    private AdminAwardRow mapAward(ResultSet resultSet, int rowNumber) throws SQLException {
        return new AdminAwardRow(
                resultSet.getObject("id", UUID.class),
                resultSet.getString("title"),
                resultSet.getString("issuer"),
                resultSet.getObject("award_date", LocalDate.class),
                resultSet.getString("description"),
                resultSet.getBoolean("cv_include"),
                resultSet.getLong("version"),
                resultSet.getObject("created_at", OffsetDateTime.class),
                resultSet.getObject("updated_at", OffsetDateTime.class));
    }

    private AdminActivityRow mapActivity(ResultSet resultSet, int rowNumber) throws SQLException {
        return new AdminActivityRow(
                resultSet.getObject("id", UUID.class),
                resultSet.getString("activity_name"),
                resultSet.getString("role_title"),
                resultSet.getObject("start_date", LocalDate.class),
                resultSet.getObject("end_date", LocalDate.class),
                resultSet.getString("description"),
                resultSet.getBoolean("cv_include"),
                resultSet.getLong("version"),
                resultSet.getObject("created_at", OffsetDateTime.class),
                resultSet.getObject("updated_at", OffsetDateTime.class));
    }
}
