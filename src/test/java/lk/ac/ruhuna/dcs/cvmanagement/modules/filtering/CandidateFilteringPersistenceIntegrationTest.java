package lk.ac.ruhuna.dcs.cvmanagement.modules.filtering;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;
import lk.ac.ruhuna.dcs.cvmanagement.modules.filtering.domain.policy.FilterSkillCriteriaSource;
import lk.ac.ruhuna.dcs.cvmanagement.modules.filtering.domain.policy.FilterSkillMatchMode;
import lk.ac.ruhuna.dcs.cvmanagement.modules.filtering.persistence.entity.FilterRunEntity;
import lk.ac.ruhuna.dcs.cvmanagement.modules.filtering.persistence.entity.FilterRunSkillEntity;
import lk.ac.ruhuna.dcs.cvmanagement.modules.filtering.persistence.repository.FilterRunRepository;
import lk.ac.ruhuna.dcs.cvmanagement.modules.filtering.persistence.repository.FilterRunSkillRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

/** Verifies Candidate Filtering Flyway DDL, JPA mappings, and database integrity on PostgreSQL. */
@SpringBootTest
@ActiveProfiles("test")
@Testcontainers(disabledWithoutDocker = true)
class CandidateFilteringPersistenceIntegrationTest {

    @Container
    private static final PostgreSQLContainer<?> POSTGRES =
            new PostgreSQLContainer<>("postgres:16-alpine")
                    .withDatabaseName("cv_management_filtering_test")
                    .withUsername("cv_user")
                    .withPassword("cv_local_password");

    @DynamicPropertySource
    static void postgresProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", POSTGRES::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGRES::getUsername);
        registry.add("spring.datasource.password", POSTGRES::getPassword);
        registry.add("spring.datasource.driver-class-name", POSTGRES::getDriverClassName);
        registry.add("spring.flyway.enabled", () -> true);
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "validate");
    }

    @Autowired
    private JdbcTemplate jdbc;

    @Autowired
    private FilterRunRepository filterRunRepository;

    @Autowired
    private FilterRunSkillRepository filterRunSkillRepository;

    @Test
    void hibernateMappingsPersistNormalizedRunCriteria() {
        Fixture fixture = createFixture();
        UUID runId = UUID.randomUUID();
        OffsetDateTime createdAt = OffsetDateTime.parse("2026-08-17T03:30:00Z");

        FilterRunEntity run = new FilterRunEntity();
        run.setId(runId);
        run.setInternshipRequestId(fixture.requestId());
        run.setRunByAccountId(fixture.adminAccountId());
        run.setRuntimeGpaLowerBound(new BigDecimal("2.75"));
        run.setRuntimeGpaUpperBound(new BigDecimal("4.00"));
        run.setSkillMatchMode(FilterSkillMatchMode.AND);
        run.setCreatedAt(createdAt);
        filterRunRepository.saveAndFlush(run);

        filterRunSkillRepository.saveAndFlush(
                new FilterRunSkillEntity(runId, fixture.skillId(), FilterSkillCriteriaSource.REQUEST));

        FilterRunEntity persisted = filterRunRepository.findById(runId).orElseThrow();
        assertThat(persisted.getInternshipRequestId()).isEqualTo(fixture.requestId());
        assertThat(persisted.getRunByAccountId()).isEqualTo(fixture.adminAccountId());
        assertThat(persisted.getRuntimeGpaLowerBound()).isEqualByComparingTo("2.75");
        assertThat(persisted.getRuntimeGpaUpperBound()).isEqualByComparingTo("4.00");
        assertThat(persisted.getSkillMatchMode()).isEqualTo(FilterSkillMatchMode.AND);
        assertThat(persisted.getCreatedAt()).isEqualTo(createdAt);

        assertThat(filterRunSkillRepository.findAllByFilterRunId(runId))
                .singleElement()
                .satisfies(skill -> {
                    assertThat(skill.getId().getSkillId()).isEqualTo(fixture.skillId());
                    assertThat(skill.getCriteriaSource()).isEqualTo(FilterSkillCriteriaSource.REQUEST);
                });
    }

    @Test
    void databaseRejectsInvalidRunCriteriaAndDuplicateSkillRows() {
        Fixture fixture = createFixture();

        assertThatThrownBy(() -> insertRun(
                        UUID.randomUUID(), fixture, new BigDecimal("4.01"), null, "AND"))
                .isInstanceOf(DataIntegrityViolationException.class);

        assertThatThrownBy(() -> insertRun(
                        UUID.randomUUID(), fixture, new BigDecimal("3.50"), new BigDecimal("3.00"), "AND"))
                .isInstanceOf(DataIntegrityViolationException.class);

        assertThatThrownBy(() -> insertRun(
                        UUID.randomUUID(), fixture, null, null, "ANY"))
                .isInstanceOf(DataIntegrityViolationException.class);

        UUID runId = UUID.randomUUID();
        insertRun(runId, fixture, null, null, "OR");

        assertThatThrownBy(() -> jdbc.update(
                        "INSERT INTO candidate_filter_run_skills (filter_run_id, skill_id, criteria_source) "
                                + "VALUES (?, ?, 'OTHER')",
                        runId,
                        fixture.skillId()))
                .isInstanceOf(DataIntegrityViolationException.class);

        jdbc.update(
                "INSERT INTO candidate_filter_run_skills (filter_run_id, skill_id, criteria_source) "
                        + "VALUES (?, ?, 'ADDITIONAL')",
                runId,
                fixture.skillId());

        assertThatThrownBy(() -> jdbc.update(
                        "INSERT INTO candidate_filter_run_skills (filter_run_id, skill_id, criteria_source) "
                                + "VALUES (?, ?, 'REQUEST')",
                        runId,
                        fixture.skillId()))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void deletingInternshipRequestCascadesFilteringHistoryWithoutDeletingCanonicalSkill() {
        Fixture fixture = createFixture();
        UUID runId = UUID.randomUUID();
        insertRun(runId, fixture, new BigDecimal("3.00"), null, "AND");
        jdbc.update(
                "INSERT INTO candidate_filter_run_skills (filter_run_id, skill_id, criteria_source) "
                        + "VALUES (?, ?, 'REQUEST')",
                runId,
                fixture.skillId());

        jdbc.update("DELETE FROM internship_requests WHERE id = ?", fixture.requestId());

        assertThat(count("SELECT COUNT(*) FROM candidate_filter_runs WHERE id = ?", runId)).isZero();
        assertThat(count(
                        "SELECT COUNT(*) FROM candidate_filter_run_skills WHERE filter_run_id = ?",
                        runId))
                .isZero();
        assertThat(count("SELECT COUNT(*) FROM skills WHERE id = ?", fixture.skillId())).isEqualTo(1);
    }

    private Fixture createFixture() {
        UUID adminAccountId = UUID.randomUUID();
        UUID companyId = UUID.randomUUID();
        UUID requestId = UUID.randomUUID();
        UUID skillId = jdbc.queryForObject(
                "SELECT id FROM skills WHERE skill_status = 'ACTIVE' ORDER BY skill_name, id LIMIT 1",
                UUID.class);

        jdbc.update(
                "INSERT INTO user_accounts (id, university_email, account_status) VALUES (?, ?, 'ACTIVE')",
                adminAccountId,
                "filtering-admin-" + adminAccountId + "@dcs.ruh.ac.lk");
        jdbc.update(
                "INSERT INTO companies (id, name) VALUES (?, ?)",
                companyId,
                "Filtering Test Company " + companyId);
        jdbc.update(
                "INSERT INTO internship_requests (id, company_id, title, created_by_account_id) "
                        + "VALUES (?, ?, ?, ?)",
                requestId,
                companyId,
                "Filtering Persistence Test",
                adminAccountId);

        return new Fixture(adminAccountId, requestId, skillId);
    }

    private void insertRun(
            UUID runId,
            Fixture fixture,
            BigDecimal lowerBound,
            BigDecimal upperBound,
            String skillMatchMode) {
        jdbc.update(
                "INSERT INTO candidate_filter_runs "
                        + "(id, internship_request_id, run_by_account_id, runtime_gpa_lower_bound, "
                        + "runtime_gpa_upper_bound, skill_match_mode) VALUES (?, ?, ?, ?, ?, ?)",
                runId,
                fixture.requestId(),
                fixture.adminAccountId(),
                lowerBound,
                upperBound,
                skillMatchMode);
    }

    private int count(String sql, Object... arguments) {
        Integer value = jdbc.queryForObject(sql, Integer.class, arguments);
        return value == null ? 0 : value;
    }

    private record Fixture(UUID adminAccountId, UUID requestId, UUID skillId) {
    }
}
