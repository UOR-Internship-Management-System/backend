package lk.ac.ruhuna.dcs.cvmanagement.modules.adminstudents.persistence.query;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import lk.ac.ruhuna.dcs.cvmanagement.modules.adminstudents.persistence.projection.AdminProjectRow;
import lk.ac.ruhuna.dcs.cvmanagement.modules.adminstudents.persistence.projection.AdminProjectSkillRow;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

/**
 * Read-only project projection for Admin inspection.
 *
 * <p>Project skills are loaded in one bounded batch for the page to avoid one query per project.
 */
@Repository
public class AdminProjectReadRepository {

    public static final String SORT = "updatedAt,desc";

    private final NamedParameterJdbcTemplate jdbcTemplate;

    public AdminProjectReadRepository(NamedParameterJdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public Page<AdminProjectRow> search(UUID studentId, String normalizedSearch, int page, int size) {
        MapSqlParameterSource parameters = new MapSqlParameterSource("studentId", studentId);
        String searchPredicate = "";
        if (normalizedSearch != null) {
            searchPredicate = """
                     AND (LOWER(p.title) LIKE :searchPattern ESCAPE '\\'
                          OR LOWER(COALESCE(p.description, '')) LIKE :searchPattern ESCAPE '\\')
                    """;
            parameters.addValue("searchPattern", likePattern(normalizedSearch));
        }

        String where = " WHERE p.student_id = :studentId " + searchPredicate;
        Long total = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM public.student_projects p" + where,
                parameters,
                Long.class);

        parameters.addValue("limit", size);
        parameters.addValue("offset", Math.multiplyExact((long) page, (long) size));

        String sql = """
                SELECT p.id AS project_id,
                       p.title,
                       p.description,
                       p.repository_url,
                       p.demo_url,
                       p.start_date,
                       p.end_date,
                       p.include_in_cv,
                       p.version,
                       p.created_at,
                       p.updated_at
                FROM public.student_projects p
                """ + where
                + " ORDER BY p.updated_at DESC, p.id ASC"
                + " LIMIT :limit OFFSET :offset";

        List<AdminProjectRow> items = jdbcTemplate.query(sql, parameters, this::mapProject);
        return new PageImpl<>(items, PageRequest.of(page, size), total == null ? 0L : total);
    }

    public List<AdminProjectSkillRow> findSkills(UUID studentId, Collection<UUID> projectIds) {
        if (projectIds.isEmpty()) {
            return List.of();
        }

        MapSqlParameterSource parameters = new MapSqlParameterSource()
                .addValue("studentId", studentId)
                .addValue("projectIds", projectIds);

        return jdbcTemplate.query(
                """
                SELECT ps.project_id,
                       s.id AS skill_id,
                       s.skill_name,
                       s.skill_description
                FROM public.student_project_skills ps
                JOIN public.student_projects p ON p.id = ps.project_id
                JOIN public.skills s ON s.id = ps.skill_id
                WHERE p.student_id = :studentId
                  AND ps.project_id IN (:projectIds)
                ORDER BY ps.project_id ASC, LOWER(s.skill_name) ASC, s.id ASC
                """,
                parameters,
                this::mapSkill);
    }

    private AdminProjectRow mapProject(ResultSet resultSet, int rowNumber) throws SQLException {
        return new AdminProjectRow(
                resultSet.getObject("project_id", UUID.class),
                resultSet.getString("title"),
                resultSet.getString("description"),
                resultSet.getString("repository_url"),
                resultSet.getString("demo_url"),
                resultSet.getObject("start_date", LocalDate.class),
                resultSet.getObject("end_date", LocalDate.class),
                resultSet.getBoolean("include_in_cv"),
                resultSet.getLong("version"),
                resultSet.getObject("created_at", OffsetDateTime.class),
                resultSet.getObject("updated_at", OffsetDateTime.class));
    }

    private AdminProjectSkillRow mapSkill(ResultSet resultSet, int rowNumber) throws SQLException {
        return new AdminProjectSkillRow(
                resultSet.getObject("project_id", UUID.class),
                resultSet.getObject("skill_id", UUID.class),
                resultSet.getString("skill_name"),
                resultSet.getString("skill_description"));
    }

    private String likePattern(String value) {
        String escaped = value.replace("\\", "\\\\").replace("%", "\\%").replace("_", "\\_");
        return "%" + escaped + "%";
    }
}
