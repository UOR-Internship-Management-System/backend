package lk.ac.ruhuna.dcs.cvmanagement.modules.internships;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import lk.ac.ruhuna.dcs.cvmanagement.modules.admindashboard.persistence.AdminDashboardMetricsQuery;
import lk.ac.ruhuna.dcs.cvmanagement.modules.companies.api.dto.request.CompanyRequest;
import lk.ac.ruhuna.dcs.cvmanagement.modules.companies.api.dto.request.CompanyUpdateRequest;
import lk.ac.ruhuna.dcs.cvmanagement.modules.companies.api.dto.response.CompanyResponse;
import lk.ac.ruhuna.dcs.cvmanagement.modules.companies.application.CompanyService;
import lk.ac.ruhuna.dcs.cvmanagement.modules.internships.api.dto.request.InternshipRequestCreateRequest;
import lk.ac.ruhuna.dcs.cvmanagement.modules.internships.api.dto.request.InternshipRequestSearchCriteria;
import lk.ac.ruhuna.dcs.cvmanagement.modules.internships.api.dto.request.InternshipRequestUpdateRequest;
import lk.ac.ruhuna.dcs.cvmanagement.modules.internships.api.dto.request.InternshipRequiredSkillRequest;
import lk.ac.ruhuna.dcs.cvmanagement.modules.internships.api.dto.response.InternshipRequestResponse;
import lk.ac.ruhuna.dcs.cvmanagement.modules.internships.application.InternshipRequestService;
import lk.ac.ruhuna.dcs.cvmanagement.modules.internships.domain.exception.InvalidTaxonomySkillException;
import lk.ac.ruhuna.dcs.cvmanagement.shared.error.PreconditionFailedException;
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

/**
 * PostgreSQL acceptance coverage for the completed Company and Internship Request backend.
 *
 * <p>The test intentionally exercises application services rather than only SQL constraints so that
 * transaction boundaries, optimistic locking, audit persistence, read models and database cascades
 * are verified together against the same PostgreSQL dialect used in production.
 */
@SpringBootTest
@ActiveProfiles("test")
@Testcontainers(disabledWithoutDocker = true)
class CompanyInternshipPostgresAcceptanceTest {

    private static final UUID ADMIN_ID = UUID.fromString("d0000000-0000-4000-8000-000000000001");
    private static final UUID MISSING_ADMIN_ID = UUID.fromString("d0000000-0000-4000-8000-000000000099");
    private static final String ADMIN_EMAIL = "patch7.admin@dcs.ruh.ac.lk";

    private static final UUID REACT_SKILL_ID = UUID.fromString("c0000000-0000-0000-0000-000000000001");
    private static final UUID TYPESCRIPT_SKILL_ID = UUID.fromString("c0000000-0000-0000-0000-000000000002");
    private static final UUID SPRING_BOOT_SKILL_ID = UUID.fromString("c0000000-0000-0000-0000-000000000003");

    @Container
    private static final PostgreSQLContainer<?> POSTGRES =
            new PostgreSQLContainer<>("postgres:16-alpine")
                    .withDatabaseName("cv_management_company_internship_acceptance")
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
    private CompanyService companyService;

    @Autowired
    private InternshipRequestService internshipRequestService;

    @Autowired
    private AdminDashboardMetricsQuery adminDashboardMetricsQuery;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void setUpDatabaseAndActor() {
        SecurityContextHolder.clearContext();
        jdbcTemplate.update("DELETE FROM public.internship_request_skills");
        jdbcTemplate.update("DELETE FROM public.internship_requests");
        jdbcTemplate.update("DELETE FROM public.companies");
        jdbcTemplate.update("DELETE FROM public.audit_events WHERE event_category = 'INTERNSHIP_MANAGEMENT'");
        jdbcTemplate.update("DELETE FROM public.user_roles WHERE user_id = ?", ADMIN_ID);
        jdbcTemplate.update("DELETE FROM public.user_accounts WHERE id = ?", ADMIN_ID);
        jdbcTemplate.update(
                "INSERT INTO public.user_accounts (id, university_email, account_status) VALUES (?, ?, 'ACTIVE')",
                ADMIN_ID,
                ADMIN_EMAIL);
        jdbcTemplate.update(
                "UPDATE public.skills SET skill_status = 'ACTIVE' WHERE id IN (?, ?, ?)",
                REACT_SKILL_ID,
                TYPESCRIPT_SKILL_ID,
                SPRING_BOOT_SKILL_ID);
        authenticate(ADMIN_ID, ADMIN_EMAIL);
    }

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void companyDeleteCascadesRequestsAndAssociationsPreservesTaxonomyAndUpdatesDashboardMetric() {
        CompanyResponse company = createCompany("Cascade Acceptance Company");
        InternshipRequestResponse request = createRequest(
                company.companyId(),
                "Platform Engineering Intern",
                List.of(REACT_SKILL_ID, TYPESCRIPT_SKILL_ID));

        assertThat(adminDashboardMetricsQuery.countInternshipRequests()).isEqualTo(1L);
        assertThat(count("SELECT COUNT(*) FROM public.internship_request_skills WHERE internship_request_id = ?",
                request.requestId())).isEqualTo(2);

        companyService.delete(company.companyId(), company.version());

        assertThat(count("SELECT COUNT(*) FROM public.companies WHERE id = ?", company.companyId())).isZero();
        assertThat(count("SELECT COUNT(*) FROM public.internship_requests WHERE id = ?", request.requestId())).isZero();
        assertThat(count(
                "SELECT COUNT(*) FROM public.internship_request_skills WHERE internship_request_id = ?",
                request.requestId())).isZero();
        assertThat(count("SELECT COUNT(*) FROM public.skills WHERE id IN (?, ?)", REACT_SKILL_ID, TYPESCRIPT_SKILL_ID))
                .isEqualTo(2);
        assertThat(adminDashboardMetricsQuery.countInternshipRequests()).isZero();

        assertThat(auditEventTypes(company.companyId().toString()))
                .containsExactly("COMPANY_CREATED", "COMPANY_DELETED");
        assertThat(auditEventTypes(request.requestId().toString()))
                .containsExactly("INTERNSHIP_REQUEST_CREATED");
    }

    @Test
    void requestUpdateReplacesSkillsAtomicallyAndInvalidTaxonomySelectionLeavesCommittedStateUntouched() {
        CompanyResponse company = createCompany("Atomic Replacement Company");
        InternshipRequestResponse created = createRequest(
                company.companyId(),
                "Backend Intern",
                List.of(REACT_SKILL_ID));

        InternshipRequestUpdateRequest update = new InternshipRequestUpdateRequest();
        update.setTitle("Backend Platform Intern");
        update.setRequiredSkills(List.of(
                new InternshipRequiredSkillRequest(TYPESCRIPT_SKILL_ID),
                new InternshipRequiredSkillRequest(SPRING_BOOT_SKILL_ID)));
        InternshipRequestResponse updated = internshipRequestService.update(created.requestId(), update, created.version());

        assertThat(updated.version()).isEqualTo(created.version() + 1);
        assertThat(updated.title()).isEqualTo("Backend Platform Intern");
        assertThat(updated.requiredSkills())
                .extracting(skill -> skill.skillId())
                .containsExactlyInAnyOrder(TYPESCRIPT_SKILL_ID, SPRING_BOOT_SKILL_ID);

        jdbcTemplate.update("UPDATE public.skills SET skill_status = 'INACTIVE' WHERE id = ?", REACT_SKILL_ID);
        InternshipRequestUpdateRequest invalidUpdate = new InternshipRequestUpdateRequest();
        invalidUpdate.setTitle("This title must not commit");
        invalidUpdate.setRequiredSkills(List.of(new InternshipRequiredSkillRequest(REACT_SKILL_ID)));

        assertThatThrownBy(() -> internshipRequestService.update(
                        created.requestId(), invalidUpdate, updated.version()))
                .isInstanceOf(InvalidTaxonomySkillException.class);

        InternshipRequestResponse persisted = internshipRequestService.get(created.requestId());
        assertThat(persisted.version()).isEqualTo(updated.version());
        assertThat(persisted.title()).isEqualTo("Backend Platform Intern");
        assertThat(persisted.requiredSkills())
                .extracting(skill -> skill.skillId())
                .containsExactlyInAnyOrder(TYPESCRIPT_SKILL_ID, SPRING_BOOT_SKILL_ID);
        assertThat(auditEventTypes(created.requestId().toString()))
                .containsExactly(
                        "INTERNSHIP_REQUEST_CREATED",
                        "INTERNSHIP_REQUEST_UPDATED",
                        "INTERNSHIP_REQUIRED_SKILLS_REPLACED");
    }

    @Test
    void optimisticConcurrencyProtectsCompanyRequestAndNestedSkillMutations() {
        CompanyResponse company = createCompany("Concurrency Acceptance Company");

        CompanyUpdateRequest companyUpdate = new CompanyUpdateRequest();
        companyUpdate.setNotes("version one");
        CompanyResponse updatedCompany = companyService.update(company.companyId(), companyUpdate, company.version());
        assertThat(updatedCompany.version()).isEqualTo(company.version() + 1);

        CompanyUpdateRequest staleCompanyUpdate = new CompanyUpdateRequest();
        staleCompanyUpdate.setNotes("stale change");
        assertThatThrownBy(() -> companyService.update(
                        company.companyId(), staleCompanyUpdate, company.version()))
                .isInstanceOf(PreconditionFailedException.class);

        InternshipRequestResponse request = createRequest(
                company.companyId(),
                "Concurrency Intern",
                List.of(REACT_SKILL_ID));
        InternshipRequestUpdateRequest requestUpdate = new InternshipRequestUpdateRequest();
        requestUpdate.setTitle("Concurrency Intern Updated");
        InternshipRequestResponse updatedRequest = internshipRequestService.update(
                request.requestId(), requestUpdate, request.version());
        assertThat(updatedRequest.version()).isEqualTo(request.version() + 1);

        InternshipRequestUpdateRequest staleRequestUpdate = new InternshipRequestUpdateRequest();
        staleRequestUpdate.setTitle("Stale Request Update");
        assertThatThrownBy(() -> internshipRequestService.update(
                        request.requestId(), staleRequestUpdate, request.version()))
                .isInstanceOf(PreconditionFailedException.class);

        var added = internshipRequestService.addRequiredSkill(
                request.requestId(),
                new InternshipRequiredSkillRequest(TYPESCRIPT_SKILL_ID),
                updatedRequest.version());
        assertThat(added.requestVersion()).isEqualTo(updatedRequest.version() + 1);

        assertThatThrownBy(() -> internshipRequestService.addRequiredSkill(
                        request.requestId(),
                        new InternshipRequiredSkillRequest(SPRING_BOOT_SKILL_ID),
                        updatedRequest.version()))
                .isInstanceOf(PreconditionFailedException.class);

        InternshipRequestResponse persisted = internshipRequestService.get(request.requestId());
        assertThat(persisted.version()).isEqualTo(added.requestVersion());
        assertThat(persisted.requiredSkills())
                .extracting(skill -> skill.skillId())
                .containsExactlyInAnyOrder(REACT_SKILL_ID, TYPESCRIPT_SKILL_ID);
    }

    @Test
    void serverSideSearchCompanyFilterAndSortUseTheRealPostgresReadModel() {
        CompanyResponse zeta = createCompany("Zeta Labs");
        CompanyResponse alpha = createCompany("Alpha Labs");
        createRequest(zeta.companyId(), "100% Platform Internship", List.of(REACT_SKILL_ID));
        createRequest(alpha.companyId(), "Backend Engineering Internship", List.of(SPRING_BOOT_SKILL_ID));

        var byCompany = internshipRequestService.list(
                new InternshipRequestSearchCriteria(0, 20, "companyName,asc", null, null));
        assertThat(byCompany.items())
                .extracting(item -> item.company().name())
                .containsExactly("Alpha Labs", "Zeta Labs");

        var filtered = internshipRequestService.list(
                new InternshipRequestSearchCriteria(0, 20, "createdAt,desc", null, alpha.companyId()));
        assertThat(filtered.items()).hasSize(1);
        assertThat(filtered.items().getFirst().company().companyId()).isEqualTo(alpha.companyId());

        var literalPercentSearch = internshipRequestService.list(
                new InternshipRequestSearchCriteria(0, 20, "title,asc", "%", null));
        assertThat(literalPercentSearch.items())
                .extracting(InternshipRequestResponse::title)
                .containsExactly("100% Platform Internship");

        var companyNameSearch = internshipRequestService.list(
                new InternshipRequestSearchCriteria(0, 20, "title,asc", "alpha", null));
        assertThat(companyNameSearch.items())
                .extracting(InternshipRequestResponse::title)
                .containsExactly("Backend Engineering Internship");
    }

    @Test
    void requiredAuditFailureRollsBackCompanyMutation() {
        authenticate(MISSING_ADMIN_ID, "missing.patch7.admin@dcs.ruh.ac.lk");

        assertThatThrownBy(() -> companyService.create(new CompanyRequest(
                        "Audit Rollback Company", null, null, null, null, null)))
                .isInstanceOf(DataIntegrityViolationException.class);

        assertThat(count(
                "SELECT COUNT(*) FROM public.companies WHERE normalized_name = 'audit rollback company'"))
                .isZero();
    }

    private CompanyResponse createCompany(String name) {
        return companyService.create(new CompanyRequest(
                name,
                "https://example.com/" + name.toLowerCase().replace(' ', '-'),
                "HR Contact",
                "hr@" + name.toLowerCase().replace(" ", "") + ".example",
                "+94 11 555 0101",
                "Patch 7 PostgreSQL acceptance data"));
    }

    private InternshipRequestResponse createRequest(UUID companyId, String title, List<UUID> skillIds) {
        return internshipRequestService.create(new InternshipRequestCreateRequest(
                companyId,
                title,
                "Patch 7 PostgreSQL acceptance request",
                10,
                skillIds.stream().map(InternshipRequiredSkillRequest::new).toList()));
    }

    private List<String> auditEventTypes(String resourceId) {
        return jdbcTemplate.queryForList(
                """
                SELECT event_type
                FROM public.audit_events
                WHERE event_category = 'INTERNSHIP_MANAGEMENT'
                  AND resource_id = ?
                ORDER BY occurred_at, event_type
                """,
                String.class,
                resourceId);
    }

    private int count(String sql, Object... arguments) {
        Integer value = jdbcTemplate.queryForObject(sql, Integer.class, arguments);
        return value == null ? 0 : value;
    }

    private void authenticate(UUID actorId, String email) {
        CurrentActor actor = new CurrentActor(actorId, email, Set.of(RoleName.ADMIN));
        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                actor,
                null,
                List.of(new SimpleGrantedAuthority(RoleName.ADMIN.authority())));
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }
}
