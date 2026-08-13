package lk.ac.ruhuna.dcs.cvmanagement.modules.academics.persistence;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lk.ac.ruhuna.dcs.cvmanagement.modules.academics.api.dto.response.AcademicRecordResponse;
import lk.ac.ruhuna.dcs.cvmanagement.modules.academics.domain.AcademicRecordSort;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

/** Read-optimized committed academic-record projection with server-controlled SQL ordering. */
@Repository
public class AcademicRecordQueryRepository {

    private static final String FROM_AND_JOIN = """
            FROM academic.official_student_grade g
            JOIN academic.subject s ON s.subject_id = g.subject_id
            """;

    private final NamedParameterJdbcTemplate jdbcTemplate;

    public AcademicRecordQueryRepository(NamedParameterJdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public Page<AcademicRecordResponse> search(
            String normalizedSearch,
            String courseCode,
            UUID studentId,
            int page,
            int size,
            AcademicRecordSort sort) {
        MapSqlParameterSource parameters = new MapSqlParameterSource();
        List<String> predicates = new ArrayList<>();

        if (normalizedSearch != null) {
            predicates.add("(LOWER(s.course_code) LIKE :searchPattern ESCAPE '\\' "
                    + "OR LOWER(s.course_title) LIKE :searchPattern ESCAPE '\\')");
            parameters.addValue("searchPattern", likePattern(normalizedSearch));
        }
        if (courseCode != null) {
            predicates.add("s.course_code = :courseCode");
            parameters.addValue("courseCode", courseCode);
        }
        if (studentId != null) {
            predicates.add("g.student_id = :studentId");
            parameters.addValue("studentId", studentId);
        }

        String where = predicates.isEmpty() ? "" : " WHERE " + String.join(" AND ", predicates);
        Long total = jdbcTemplate.queryForObject("SELECT COUNT(*) " + FROM_AND_JOIN + where, parameters, Long.class);
        long totalElements = total == null ? 0L : total;

        parameters.addValue("limit", size);
        parameters.addValue("offset", Math.multiplyExact(page, size));
        String dataSql = """
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
                + " ORDER BY " + sort.sqlOrder() + ", g.official_student_grade_id ASC"
                + " LIMIT :limit OFFSET :offset";

        List<AcademicRecordResponse> items = jdbcTemplate.query(dataSql, parameters, this::mapRecord);
        return new PageImpl<>(items, PageRequest.of(page, size), totalElements);
    }

    private AcademicRecordResponse mapRecord(ResultSet resultSet, int rowNumber) throws SQLException {
        return new AcademicRecordResponse(
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
