package lk.ac.ruhuna.dcs.cvmanagement.modules.academics.persistence;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

/** Module-owned read adapter over the authoritative eligible-Student table. */
@Repository
public class AcademicStudentReferenceQuery {

    private final NamedParameterJdbcTemplate jdbcTemplate;

    public AcademicStudentReferenceQuery(NamedParameterJdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public Map<String, StudentReference> findByIndexNumbers(Collection<String> indexNumbers) {
        if (indexNumbers == null || indexNumbers.isEmpty()) {
            return Collections.emptyMap();
        }
        var parameters = new MapSqlParameterSource("indexNumbers", indexNumbers);
        return jdbcTemplate.query(
                        """
                        SELECT id, index_number, is_active
                        FROM public.eligible_students
                        WHERE index_number IN (:indexNumbers)
                        """,
                        parameters,
                        (rs, rowNum) -> new StudentReference(
                                rs.getObject("id", UUID.class),
                                rs.getString("index_number"),
                                rs.getBoolean("is_active")))
                .stream()
                .collect(Collectors.toUnmodifiableMap(StudentReference::indexNumber, Function.identity()));
    }

    public record StudentReference(UUID studentId, String indexNumber, boolean active) {
    }
}
