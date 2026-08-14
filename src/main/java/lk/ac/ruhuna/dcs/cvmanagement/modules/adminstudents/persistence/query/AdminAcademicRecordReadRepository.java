package lk.ac.ruhuna.dcs.cvmanagement.modules.adminstudents.persistence.query;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lk.ac.ruhuna.dcs.cvmanagement.modules.adminstudents.domain.model.AdminAcademicRecordSort;
import lk.ac.ruhuna.dcs.cvmanagement.modules.adminstudents.persistence.projection.AdminAcademicRecordRow;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

/** Read-only committed academic-record projection owned by the Admin Student module. */
@Repository
public class AdminAcademicRecordReadRepository {

    private static final String FROM_AND_JOIN = """
            FROM academic.official_student_grade g
            JOIN academic.subject s ON s.subject_id = g.subject_id
            """;

    private final NamedParameterJdbcTemplate jdbcTemplate;

    public AdminAcademicRecordReadRepository(NamedParameterJdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public Page<AdminAcademicRecordRow> search(
            UUID studentId,
            String normalizedSearch,
            String courseCode,
            int page,
            int size,
            AdminAcademicRecordSort sort) {
        MapSqlParameterSource parameters = new MapSqlParameterSource("studentId", studentId);
        List<String> predicates = new ArrayList<>();
        predicates.add("g.student_id = :studentId");

        if (normalizedSearch != null) {
            predicates.add("(LOWER(s.course_code) LIKE :searchPattern ESCAPE '\\' "
                    + "OR LOWER(s.course_title) LIKE :searchPattern ESCAPE '\\')");
            parameters.addValue("searchPattern", likePattern(normalizedSearch));
        }
        if (courseCode != null) {
            predicates.add("s.course_code = :courseCode");
            parameters.addValue("courseCode", courseCode);
        }

        String where = " WHERE " + String.join(" AND ", predicates);
        Long total = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) " + FROM_AND_JOIN + where,
                parameters,
                Long.class);

        parameters.addValue("limit", size);
        parameters.addValue("offset", Math.multiplyExact((long) page, (long) size));

        String sql = """
                SELECT g.official_student_grade_id,
                       g.subject_id,
                       s.course_code,
                       s.course_title,
                       g.credits,
                       g.letter_grade,
                       g.grade_point,
                       g.semester,
                       g.academic_year,
                       g.attempt_number,
                       g.result_status,
                       g.committed_at
                """ + FROM_AND_JOIN + where
                + " ORDER BY " + sort.sqlOrder()
                + " LIMIT :limit OFFSET :offset";

        List<AdminAcademicRecordRow> items = jdbcTemplate.query(sql, parameters, this::mapRow);
        return new PageImpl<>(items, PageRequest.of(page, size), total == null ? 0L : total);
    }

    private AdminAcademicRecordRow mapRow(ResultSet resultSet, int rowNumber) throws SQLException {
        return new AdminAcademicRecordRow(
                resultSet.getObject("official_student_grade_id", UUID.class),
                resultSet.getObject("subject_id", UUID.class),
                resultSet.getString("course_code"),
                resultSet.getString("course_title"),
                resultSet.getBigDecimal("credits"),
                resultSet.getString("letter_grade"),
                resultSet.getBigDecimal("grade_point"),
                resultSet.getString("semester"),
                resultSet.getString("academic_year"),
                resultSet.getInt("attempt_number"),
                resultSet.getString("result_status"),
                resultSet.getObject("committed_at", OffsetDateTime.class));
    }

    private String likePattern(String value) {
        String escaped = value.replace("\\", "\\\\").replace("%", "\\%").replace("_", "\\_");
        return "%" + escaped + "%";
    }
}
