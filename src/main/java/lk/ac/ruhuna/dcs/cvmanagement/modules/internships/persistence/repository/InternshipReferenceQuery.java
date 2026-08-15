package lk.ac.ruhuna.dcs.cvmanagement.modules.internships.persistence.repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
import lk.ac.ruhuna.dcs.cvmanagement.modules.internships.persistence.projection.CompanySnapshotProjection;
import lk.ac.ruhuna.dcs.cvmanagement.modules.internships.persistence.projection.SkillSnapshotProjection;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

/**
 * Module-owned read adapter over Company and taxonomy reference tables.
 *
 * <p>This deliberately avoids Java dependencies on the Companies and Skills modules while the
 * database foreign keys remain the final referential-integrity boundary.
 */
@Repository
public class InternshipReferenceQuery {

    private final NamedParameterJdbcTemplate jdbcTemplate;

    public InternshipReferenceQuery(NamedParameterJdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public Optional<CompanySnapshotProjection> findCompany(UUID companyId) {
        if (companyId == null) {
            return Optional.empty();
        }
        return jdbcTemplate.query(
                        """
                        SELECT id, name, website_url, contact_person, contact_email, contact_phone,
                               notes, version, created_at, updated_at
                        FROM public.companies
                        WHERE id = :companyId
                        """,
                        new MapSqlParameterSource("companyId", companyId),
                        this::mapCompany)
                .stream()
                .findFirst();
    }

    public Map<UUID, SkillSnapshotProjection> findSkills(Collection<UUID> skillIds) {
        if (skillIds == null || skillIds.isEmpty()) {
            return Collections.emptyMap();
        }
        return jdbcTemplate.query(
                        """
                        SELECT id, skill_name, skill_status
                        FROM public.skills
                        WHERE id IN (:skillIds)
                        """,
                        new MapSqlParameterSource("skillIds", skillIds),
                        (rs, rowNum) -> new SkillSnapshotProjection(
                                rs.getObject("id", UUID.class),
                                rs.getString("skill_name"),
                                "ACTIVE".equals(rs.getString("skill_status"))))
                .stream()
                .collect(Collectors.toUnmodifiableMap(SkillSnapshotProjection::skillId, Function.identity()));
    }

    private CompanySnapshotProjection mapCompany(ResultSet rs, int rowNumber) throws SQLException {
        return new CompanySnapshotProjection(
                rs.getObject("id", UUID.class),
                rs.getString("name"),
                rs.getString("website_url"),
                rs.getString("contact_person"),
                rs.getString("contact_email"),
                rs.getString("contact_phone"),
                rs.getString("notes"),
                rs.getLong("version"),
                rs.getObject("created_at", java.time.OffsetDateTime.class),
                rs.getObject("updated_at", java.time.OffsetDateTime.class));
    }
}
