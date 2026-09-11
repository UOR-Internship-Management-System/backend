package lk.ac.ruhuna.dcs.cvmanagement.modules.adminstudents.persistence.query;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lk.ac.ruhuna.dcs.cvmanagement.modules.adminstudents.domain.model.AdminCompetencyLevel;
import lk.ac.ruhuna.dcs.cvmanagement.modules.adminstudents.persistence.projection.AdminDeclaredSkillRow;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

/** Read-only, module-local projection of Student-declared skills for Admin inspection. */
@Repository
public class AdminDeclaredSkillReadRepository {

    public static final String SORT = "skillName,asc";

    private static final String FROM_AND_JOIN = """
            FROM public.student_declared_skills ds
            JOIN public.skills s ON s.id = ds.skill_id
            """;

    private final NamedParameterJdbcTemplate jdbcTemplate;

    public AdminDeclaredSkillReadRepository(NamedParameterJdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public Page<AdminDeclaredSkillRow> search(UUID studentId, String normalizedSearch, int page, int size) {
        MapSqlParameterSource parameters = new MapSqlParameterSource("studentId", studentId);
        List<String> predicates = new ArrayList<>();
        predicates.add("ds.student_id = :studentId");

        if (normalizedSearch != null) {
            predicates.add("LOWER(s.skill_name) LIKE :searchPattern ESCAPE '\\'");
            parameters.addValue("searchPattern", likePattern(normalizedSearch));
        }

        String where = " WHERE " + String.join(" AND ", predicates);
        Long total = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) " + FROM_AND_JOIN + where,
                parameters,
                Long.class);

        parameters.addValue("limit", size);
        parameters.addValue("offset", Math.multiplyExact((long) page, (long) size));

        String sql = """
                SELECT ds.id AS declared_skill_id,
                       ds.skill_id,
                       s.skill_name,
                       ds.competency_level,
                       ds.version,
                       ds.created_at,
                       ds.updated_at
                """ + FROM_AND_JOIN + where
                + " ORDER BY LOWER(s.skill_name) ASC, ds.id ASC"
                + " LIMIT :limit OFFSET :offset";

        List<AdminDeclaredSkillRow> items = jdbcTemplate.query(sql, parameters, this::mapRow);
        return new PageImpl<>(items, PageRequest.of(page, size), total == null ? 0L : total);
    }

    private AdminDeclaredSkillRow mapRow(ResultSet resultSet, int rowNumber) throws SQLException {
        return new AdminDeclaredSkillRow(
                resultSet.getObject("declared_skill_id", UUID.class),
                resultSet.getObject("skill_id", UUID.class),
                resultSet.getString("skill_name"),
                AdminCompetencyLevel.valueOf(resultSet.getString("competency_level")),
                resultSet.getLong("version"),
                resultSet.getObject("created_at", OffsetDateTime.class),
                resultSet.getObject("updated_at", OffsetDateTime.class));
    }

    private String likePattern(String value) {
        String escaped = value.replace("\\", "\\\\").replace("%", "\\%").replace("_", "\\_");
        return "%" + escaped + "%";
    }
}
