package lk.ac.ruhuna.dcs.cvmanagement.modules.filtering;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import lk.ac.ruhuna.dcs.cvmanagement.modules.filtering.api.dto.request.CandidateFilteringRunRequest;
import lk.ac.ruhuna.dcs.cvmanagement.modules.filtering.application.CandidateFilteringService;
import lk.ac.ruhuna.dcs.cvmanagement.modules.filtering.domain.policy.FilterSkillMatchMode;
import lk.ac.ruhuna.dcs.cvmanagement.shared.security.CurrentActor;
import lk.ac.ruhuna.dcs.cvmanagement.shared.security.RoleName;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

/** PostgreSQL transaction acceptance for Candidate Filtering run creation and required auditing. */
@SpringBootTest
@ActiveProfiles("test")
@Testcontainers(disabledWithoutDocker = true)
class CandidateFilteringApplicationPostgresIntegrationTest {

    private static final UUID ADMIN_ACCOUNT = UUID.fromString("93000000-0000-4000-8000-000000000001");
    private static final UUID COMPANY_ID = UUID.fromString("93000000-0000-4000-8000-000000000002");
    private static final UUID REQUEST_ID = UUID.fromString("93000000-0000-4000-8000-000000000003");
    private static final String AUDIT_REJECT_CONSTRAINT = "test_reject_candidate_filtering_audit";

    @Container
    private static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:16-alpine")
            .withDatabaseName("cv_management_filtering_application_test")
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

    @Autowired private JdbcTemplate jdbc;
    @Autowired private CandidateFilteringService service;

    @BeforeEach
    void setUp() {
        dropAuditRejectConstraint();
        cleanup();
        seedRequestContext();
        authenticateAdmin();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
        dropAuditRejectConstraint();
        cleanup();
    }

    @Test
    void createRunPersistsRunAndRequiredSanitizedAuditAtomically() {
        var response = service.createRun(new CandidateFilteringRunRequest(
                REQUEST_ID,
                null,
                null,
                List.of(),
                List.of(),
                FilterSkillMatchMode.OR));

        assertThat(response.request().requestId()).isEqualTo(REQUEST_ID);
        assertThat(response.candidateCount()).isZero();
        assertThat(count(
                "SELECT COUNT(*) FROM public.candidate_filter_runs WHERE id = ?",
                response.filterRunId()))
                .isEqualTo(1);
        assertThat(count(
                "SELECT COUNT(*) FROM public.candidate_filter_run_skills WHERE filter_run_id = ?",
                response.filterRunId()))
                .isZero();
        assertThat(count(
                """
                SELECT COUNT(*)
                FROM public.audit_events
                WHERE event_category = 'CANDIDATE_FILTERING'
                  AND event_type = 'CANDIDATE_FILTER_RUN_CREATED'
                  AND resource_id = ?
                """,
                response.filterRunId().toString()))
                .isEqualTo(1);

        String metadata = jdbc.queryForObject(
                """
                SELECT metadata::text
                FROM public.audit_events
                WHERE event_category = 'CANDIDATE_FILTERING'
                  AND resource_id = ?
                """,
                String.class,
                response.filterRunId().toString());
        assertThat(metadata)
                .contains("\"requestId\"")
                .contains("\"requestSkillCount\": 0")
                .contains("\"additionalSkillCount\": 0")
                .contains("\"candidateCount\": 0")
                .doesNotContain("candidateIds", "skillIds", "universityEmail");
    }

    @Test
    void requiredAuditFailureRollsBackRunCreation() {
        jdbc.execute("""
                ALTER TABLE public.audit_events
                ADD CONSTRAINT test_reject_candidate_filtering_audit
                CHECK (event_category <> 'CANDIDATE_FILTERING')
                """);

        assertThatThrownBy(() -> service.createRun(new CandidateFilteringRunRequest(
                        REQUEST_ID,
                        null,
                        null,
                        List.of(),
                        List.of(),
                        FilterSkillMatchMode.OR)))
                .isInstanceOf(DataIntegrityViolationException.class);

        assertThat(count(
                "SELECT COUNT(*) FROM public.candidate_filter_runs WHERE internship_request_id = ?",
                REQUEST_ID))
                .isZero();
    }

    private void seedRequestContext() {
        jdbc.update(
                "INSERT INTO public.user_accounts (id, university_email, account_status) VALUES (?, ?, 'ACTIVE')",
                ADMIN_ACCOUNT,
                "filtering.application.admin@dcs.ruh.ac.lk");
        jdbc.update(
                "INSERT INTO public.companies (id, name) VALUES (?, ?)",
                COMPANY_ID,
                "Candidate Filtering Application Ltd");
        jdbc.update(
                """
                INSERT INTO public.internship_requests (
                    id, company_id, title, shortlist_guidance_value, created_by_account_id
                ) VALUES (?, ?, 'Backend Engineering Intern', 5, ?)
                """,
                REQUEST_ID,
                COMPANY_ID,
                ADMIN_ACCOUNT);
    }

    private void authenticateAdmin() {
        CurrentActor actor = new CurrentActor(
                ADMIN_ACCOUNT,
                "filtering.application.admin@dcs.ruh.ac.lk",
                Set.of(RoleName.ADMIN));
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(
                actor,
                null,
                List.of(new SimpleGrantedAuthority(RoleName.ADMIN.authority()))));
    }

    private void cleanup() {
        jdbc.update("DELETE FROM public.audit_events WHERE event_category = 'CANDIDATE_FILTERING'");
        jdbc.update("DELETE FROM public.internship_requests WHERE id = ?", REQUEST_ID);
        jdbc.update("DELETE FROM public.companies WHERE id = ?", COMPANY_ID);
        jdbc.update("DELETE FROM public.user_accounts WHERE id = ?", ADMIN_ACCOUNT);
    }

    private void dropAuditRejectConstraint() {
        jdbc.execute("ALTER TABLE public.audit_events DROP CONSTRAINT IF EXISTS " + AUDIT_REJECT_CONSTRAINT);
    }

    private int count(String sql, Object... args) {
        Integer value = jdbc.queryForObject(sql, Integer.class, args);
        return value == null ? 0 : value;
    }
}
