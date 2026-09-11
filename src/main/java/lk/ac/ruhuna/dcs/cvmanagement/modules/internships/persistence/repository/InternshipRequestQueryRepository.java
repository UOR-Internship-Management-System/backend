package lk.ac.ruhuna.dcs.cvmanagement.modules.internships.persistence.repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lk.ac.ruhuna.dcs.cvmanagement.modules.internships.domain.policy.InternshipRequestSort;
import lk.ac.ruhuna.dcs.cvmanagement.modules.internships.persistence.projection.InternshipRequestDetailProjection;
import lk.ac.ruhuna.dcs.cvmanagement.modules.internships.persistence.projection.InternshipRequiredSkillProjection;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

/** Bounded JDBC read model for Internship Request list/detail and required-skill responses. */
@Repository
public class InternshipRequestQueryRepository {

    private static final String REQUEST_SELECT = """
            SELECT ir.id AS request_id,
                   c.id AS company_id,
                   c.name AS company_name,
                   c.website_url AS company_website_url,
                   c.contact_person AS company_contact_person,
                   c.contact_email AS company_contact_email,
                   c.contact_phone AS company_contact_phone,
                   c.notes AS company_notes,
                   c.version AS company_version,
                   c.created_at AS company_created_at,
                   c.updated_at AS company_updated_at,
                   ir.title,
                   ir.description,
                   ir.shortlist_guidance_value,
                   ir.version,
                   ir.created_at,
                   ir.updated_at
            FROM public.internship_requests ir
            JOIN public.companies c ON c.id = ir.company_id
            """;

    private final NamedParameterJdbcTemplate jdbcTemplate;

    public InternshipRequestQueryRepository(NamedParameterJdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public Page<InternshipRequestDetailProjection> search(
            String normalizedSearch,
            UUID companyId,
            int page,
            int size,
            InternshipRequestSort sort) {
        MapSqlParameterSource parameters = new MapSqlParameterSource();
        List<String> predicates = new ArrayList<>();
        if (normalizedSearch != null) {
            predicates.add("(LOWER(ir.title) LIKE :searchPattern ESCAPE '\\' "
                    + "OR LOWER(c.name) LIKE :searchPattern ESCAPE '\\')");
            parameters.addValue("searchPattern", likePattern(normalizedSearch));
        }
        if (companyId != null) {
            predicates.add("ir.company_id = :companyId");
            parameters.addValue("companyId", companyId);
        }

        String where = predicates.isEmpty() ? "" : " WHERE " + String.join(" AND ", predicates);
        Long total = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM public.internship_requests ir "
                        + "JOIN public.companies c ON c.id = ir.company_id" + where,
                parameters,
                Long.class);

        parameters.addValue("limit", size);
        parameters.addValue("offset", Math.multiplyExact((long) page, (long) size));
        List<InternshipRequestDetailProjection> items = jdbcTemplate.query(
                REQUEST_SELECT + where + " ORDER BY " + sort.sqlOrder() + " LIMIT :limit OFFSET :offset",
                parameters,
                this::mapRequest);
        return new PageImpl<>(items, PageRequest.of(page, size), total == null ? 0L : total);
    }

    public Optional<InternshipRequestDetailProjection> findDetail(UUID requestId) {
        if (requestId == null) {
            return Optional.empty();
        }
        return jdbcTemplate.query(
                        REQUEST_SELECT + " WHERE ir.id = :requestId",
                        new MapSqlParameterSource("requestId", requestId),
                        this::mapRequest)
                .stream()
                .findFirst();
    }

    public List<InternshipRequiredSkillProjection> findRequiredSkills(Collection<UUID> requestIds) {
        if (requestIds == null || requestIds.isEmpty()) {
            return Collections.emptyList();
        }
        return jdbcTemplate.query(
                """
                SELECT irs.internship_request_id AS request_id,
                       irs.id AS required_skill_id,
                       s.id AS skill_id,
                       s.skill_name
                FROM public.internship_request_skills irs
                JOIN public.skills s ON s.id = irs.skill_id
                WHERE irs.internship_request_id IN (:requestIds)
                ORDER BY LOWER(s.skill_name) ASC, s.skill_name ASC, irs.id ASC
                """,
                new MapSqlParameterSource("requestIds", requestIds),
                this::mapRequiredSkill);
    }

    public Page<InternshipRequiredSkillProjection> findRequiredSkills(
            UUID requestId, int page, int size) {
        MapSqlParameterSource parameters = new MapSqlParameterSource("requestId", requestId);
        Long total = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM public.internship_request_skills WHERE internship_request_id = :requestId",
                parameters,
                Long.class);
        parameters.addValue("limit", size);
        parameters.addValue("offset", Math.multiplyExact((long) page, (long) size));
        List<InternshipRequiredSkillProjection> rows = jdbcTemplate.query(
                """
                SELECT irs.internship_request_id AS request_id,
                       irs.id AS required_skill_id,
                       s.id AS skill_id,
                       s.skill_name
                FROM public.internship_request_skills irs
                JOIN public.skills s ON s.id = irs.skill_id
                WHERE irs.internship_request_id = :requestId
                ORDER BY LOWER(s.skill_name) ASC, s.skill_name ASC, irs.id ASC
                LIMIT :limit OFFSET :offset
                """,
                parameters,
                this::mapRequiredSkill);
        return new PageImpl<>(rows, PageRequest.of(page, size), total == null ? 0L : total);
    }

    public Optional<InternshipRequiredSkillProjection> findRequiredSkill(UUID requiredSkillId, UUID requestId) {
        return jdbcTemplate.query(
                        """
                        SELECT irs.internship_request_id AS request_id,
                               irs.id AS required_skill_id,
                               s.id AS skill_id,
                               s.skill_name
                        FROM public.internship_request_skills irs
                        JOIN public.skills s ON s.id = irs.skill_id
                        WHERE irs.id = :requiredSkillId
                          AND irs.internship_request_id = :requestId
                        """,
                        new MapSqlParameterSource()
                                .addValue("requiredSkillId", requiredSkillId)
                                .addValue("requestId", requestId),
                        this::mapRequiredSkill)
                .stream()
                .findFirst();
    }

    private InternshipRequestDetailProjection mapRequest(ResultSet rs, int rowNumber) throws SQLException {
        return new InternshipRequestDetailProjection(
                rs.getObject("request_id", UUID.class),
                rs.getObject("company_id", UUID.class),
                rs.getString("company_name"),
                rs.getString("company_website_url"),
                rs.getString("company_contact_person"),
                rs.getString("company_contact_email"),
                rs.getString("company_contact_phone"),
                rs.getString("company_notes"),
                rs.getLong("company_version"),
                rs.getObject("company_created_at", java.time.OffsetDateTime.class),
                rs.getObject("company_updated_at", java.time.OffsetDateTime.class),
                rs.getString("title"),
                rs.getString("description"),
                (Integer) rs.getObject("shortlist_guidance_value"),
                rs.getLong("version"),
                rs.getObject("created_at", java.time.OffsetDateTime.class),
                rs.getObject("updated_at", java.time.OffsetDateTime.class));
    }

    private InternshipRequiredSkillProjection mapRequiredSkill(ResultSet rs, int rowNumber) throws SQLException {
        return new InternshipRequiredSkillProjection(
                rs.getObject("request_id", UUID.class),
                rs.getObject("required_skill_id", UUID.class),
                rs.getObject("skill_id", UUID.class),
                rs.getString("skill_name"));
    }

    private String likePattern(String value) {
        String escaped = value.replace("\\", "\\\\").replace("%", "\\%").replace("_", "\\_");
        return "%" + escaped + "%";
    }
}
