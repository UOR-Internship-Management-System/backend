package lk.ac.ruhuna.dcs.cvmanagement.modules.filtering;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import lk.ac.ruhuna.dcs.cvmanagement.modules.filtering.domain.policy.CandidateFilteringCriteria;
import lk.ac.ruhuna.dcs.cvmanagement.modules.filtering.domain.policy.CandidateSort;
import lk.ac.ruhuna.dcs.cvmanagement.modules.filtering.domain.policy.FilterSkillMatchMode;
import lk.ac.ruhuna.dcs.cvmanagement.modules.filtering.persistence.projection.CandidateFilterRow;
import lk.ac.ruhuna.dcs.cvmanagement.modules.filtering.persistence.query.CandidateFilteringReadRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.InvalidDataAccessApiUsageException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

/** PostgreSQL acceptance coverage for the deterministic Candidate Filtering read model. */
@SpringBootTest
@ActiveProfiles("test")
@Testcontainers(disabledWithoutDocker = true)
class CandidateFilteringReadRepositoryPostgresIntegrationTest {

    private static final UUID ADMIN_ACCOUNT = UUID.fromString("81000000-0000-4000-8000-000000000001");
    private static final UUID FILE_ASSET = UUID.fromString("81000000-0000-4000-8000-000000000002");
    private static final UUID LEDGER_UPLOAD = UUID.fromString("81000000-0000-4000-8000-000000000003");
    private static final UUID COMPANY_ID = UUID.fromString("81000000-0000-4000-8000-000000000004");
    private static final UUID REQUEST_ID = UUID.fromString("81000000-0000-4000-8000-000000000005");
    private static final UUID PROJECT_ID = UUID.fromString("81000000-0000-4000-8000-000000000006");
    private static final UUID INACTIVE_SKILL = UUID.fromString("81000000-0000-4000-8000-000000000007");

    private static final UUID REACT = UUID.fromString("c0000000-0000-0000-0000-000000000001");
    private static final UUID TYPESCRIPT = UUID.fromString("c0000000-0000-0000-0000-000000000002");
    private static final UUID SPRING_BOOT = UUID.fromString("c0000000-0000-0000-0000-000000000003");
    private static final UUID PYTHON = UUID.fromString("c0000000-0000-0000-0000-000000000004");
    private static final UUID BACKEND_CATEGORY = UUID.fromString("b0000000-0000-0000-0000-000000000002");

    private static final UUID STUDENT_A = UUID.fromString("82000000-0000-4000-8000-000000000001");
    private static final UUID STUDENT_B = UUID.fromString("82000000-0000-4000-8000-000000000002");
    private static final UUID STUDENT_C = UUID.fromString("82000000-0000-4000-8000-000000000003");
    private static final UUID STUDENT_D = UUID.fromString("82000000-0000-4000-8000-000000000004");
    private static final UUID STUDENT_E = UUID.fromString("82000000-0000-4000-8000-000000000005");
    private static final UUID STUDENT_INACTIVE = UUID.fromString("82000000-0000-4000-8000-000000000006");
    private static final UUID STUDENT_LOCKED = UUID.fromString("82000000-0000-4000-8000-000000000007");
    private static final UUID STUDENT_NO_ROLE = UUID.fromString("82000000-0000-4000-8000-000000000008");
    private static final UUID STUDENT_TIE_ONE = UUID.fromString("82000000-0000-4000-8000-000000000009");
    private static final UUID STUDENT_TIE_TWO = UUID.fromString("82000000-0000-4000-8000-000000000010");

    private static final List<UUID> STUDENTS = List.of(
            STUDENT_A,
            STUDENT_B,
            STUDENT_C,
            STUDENT_D,
            STUDENT_E,
            STUDENT_INACTIVE,
            STUDENT_LOCKED,
            STUDENT_NO_ROLE,
            STUDENT_TIE_ONE,
            STUDENT_TIE_TWO);

    private static final List<UUID> STUDENT_ACCOUNTS = List.of(
            accountId(1), accountId(2), accountId(3), accountId(4), accountId(5),
            accountId(6), accountId(7), accountId(8), accountId(9), accountId(10));

    @Container
    private static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:16-alpine")
            .withDatabaseName("cv_management_filtering_read_test")
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
    @Autowired private CandidateFilteringReadRepository repository;

    @BeforeEach
    void seedFixtures() {
        cleanupFixtures();
        seedAdminAndAcademicSource();
        seedRequestContext();
        seedStudents();
    }

    @Test
    void readsRequestContextAndValidatesRequestAndTaxonomySkills() {
        assertThat(repository.findRequestSummary(REQUEST_ID))
                .get()
                .satisfies(row -> {
                    assertThat(row.requestId()).isEqualTo(REQUEST_ID);
                    assertThat(row.companyId()).isEqualTo(COMPANY_ID);
                    assertThat(row.companyName()).isEqualTo("Deterministic Systems Ltd");
                    assertThat(row.title()).isEqualTo("Backend Engineering Intern");
                    assertThat(row.shortlistGuidanceValue()).isEqualTo(8);
                });

        assertThat(repository.findRequestSummary(UUID.randomUUID())).isEmpty();
        assertThat(repository.findRequiredSkillIds(REQUEST_ID, List.of(REACT, TYPESCRIPT, SPRING_BOOT)))
                .containsExactlyInAnyOrder(REACT, SPRING_BOOT);
        assertThat(repository.findRequiredSkillIds(REQUEST_ID, List.of())).isEmpty();
        assertThat(repository.findActiveSkillIds(List.of(REACT, TYPESCRIPT, INACTIVE_SKILL, UUID.randomUUID())))
                .containsExactlyInAnyOrder(REACT, TYPESCRIPT);
        assertThat(repository.findActiveSkillIds(List.of())).isEmpty();
    }

    @Test
    void baseEligibilityUsesOnlyActiveRegisteredStudentAccounts() {
        var page = repository.searchCandidates(noFilters(), null, 0, 100, CandidateSort.INDEX_NUMBER_ASC);

        assertThat(page.getTotalElements()).isEqualTo(7);
        assertThat(ids(page.getContent()))
                .containsExactly(
                        STUDENT_A,
                        STUDENT_B,
                        STUDENT_C,
                        STUDENT_D,
                        STUDENT_E,
                        STUDENT_TIE_ONE,
                        STUDENT_TIE_TWO)
                .doesNotContain(STUDENT_INACTIVE, STUDENT_LOCKED, STUDENT_NO_ROLE);
        assertThat(repository.countCandidates(noFilters())).isEqualTo(7);
    }

    @Test
    void gpaBoundsAreInclusiveAndExcludeUnavailableGpaOnlyWhenABoundIsActive() {
        assertThat(ids(repository.searchCandidates(
                                criteria("3.00", null, List.of(), List.of(), FilterSkillMatchMode.OR),
                                null,
                                0,
                                100,
                                CandidateSort.INDEX_NUMBER_ASC)
                        .getContent()))
                .containsExactly(STUDENT_A, STUDENT_B, STUDENT_E, STUDENT_TIE_ONE, STUDENT_TIE_TWO);

        assertThat(ids(repository.searchCandidates(
                                criteria(null, "3.00", List.of(), List.of(), FilterSkillMatchMode.OR),
                                null,
                                0,
                                100,
                                CandidateSort.INDEX_NUMBER_ASC)
                        .getContent()))
                .containsExactly(STUDENT_B, STUDENT_C);

        assertThat(ids(repository.searchCandidates(
                                criteria("3.00", "3.50", List.of(), List.of(), FilterSkillMatchMode.OR),
                                null,
                                0,
                                100,
                                CandidateSort.INDEX_NUMBER_ASC)
                        .getContent()))
                .containsExactly(STUDENT_A, STUDENT_B, STUDENT_TIE_ONE, STUDENT_TIE_TWO);

        assertThat(repository.searchCandidates(noFilters(), null, 0, 100, CandidateSort.INDEX_NUMBER_ASC)
                        .getContent())
                .anySatisfy(row -> {
                    assertThat(row.studentId()).isEqualTo(STUDENT_D);
                    assertThat(row.officialGpa()).isNull();
                });
    }

    @Test
    void skillAndOrAndEmptyCriteriaUseOnlyCurrentStudentDeclarations() {
        CandidateFilteringCriteria andCriteria = criteria(
                null, null, List.of(REACT, SPRING_BOOT), List.of(), FilterSkillMatchMode.AND);
        assertThat(ids(repository.searchCandidates(andCriteria, null, 0, 100, CandidateSort.INDEX_NUMBER_ASC)
                        .getContent()))
                .containsExactly(STUDENT_A, STUDENT_D, STUDENT_TIE_ONE, STUDENT_TIE_TWO);

        CandidateFilteringCriteria orCriteria = criteria(
                null, null, List.of(REACT, SPRING_BOOT), List.of(), FilterSkillMatchMode.OR);
        assertThat(ids(repository.searchCandidates(orCriteria, null, 0, 100, CandidateSort.INDEX_NUMBER_ASC)
                        .getContent()))
                .containsExactly(STUDENT_A, STUDENT_B, STUDENT_C, STUDENT_D, STUDENT_TIE_ONE, STUDENT_TIE_TWO);

        assertThat(repository.searchCandidates(noFilters(), null, 0, 100, CandidateSort.INDEX_NUMBER_ASC)
                        .getTotalElements())
                .isEqualTo(7);

        // Student E has React on a portfolio project but has declared only Python. Projects must not
        // make a Student eligible for declared-skill filtering.
        assertThat(ids(repository.searchCandidates(orCriteria, null, 0, 100, CandidateSort.INDEX_NUMBER_ASC)
                        .getContent()))
                .doesNotContain(STUDENT_E);
    }

    @Test
    void combinesGpaAndSkillCriteriaDeterministically() {
        CandidateFilteringCriteria criteria = criteria(
                "3.00", "3.50", List.of(REACT, SPRING_BOOT), List.of(TYPESCRIPT), FilterSkillMatchMode.AND);

        var page = repository.searchCandidates(criteria, null, 0, 100, CandidateSort.OFFICIAL_GPA_DESC);

        assertThat(ids(page.getContent())).containsExactly(STUDENT_A);
        assertThat(repository.countCandidates(criteria)).isEqualTo(1);
    }

    @Test
    void searchUsesResolvedNameAndIndexAndTreatsSqlWildcardCharactersLiterally() {
        assertThat(repository.searchCandidates(noFilters(), "preferred alpha", 0, 20, CandidateSort.FULL_NAME_ASC)
                        .getContent())
                .singleElement()
                .satisfies(row -> {
                    assertThat(row.studentId()).isEqualTo(STUDENT_A);
                    assertThat(row.fullName()).isEqualTo("Preferred Alpha");
                });

        assertThat(repository.searchCandidates(noFilters(), "SC/2022/10002", 0, 20, CandidateSort.FULL_NAME_ASC)
                        .getContent())
                .singleElement()
                .satisfies(row -> assertThat(row.studentId()).isEqualTo(STUDENT_B));

        assertThat(repository.searchCandidates(noFilters(), "%", 0, 20, CandidateSort.FULL_NAME_ASC)
                        .getContent())
                .singleElement()
                .satisfies(row -> assertThat(row.studentId()).isEqualTo(STUDENT_E));
        assertThat(repository.searchCandidates(noFilters(), "_", 0, 20, CandidateSort.FULL_NAME_ASC)
                        .getContent())
                .singleElement()
                .satisfies(row -> assertThat(row.studentId()).isEqualTo(STUDENT_E));
    }

    @Test
    void supportsAllApprovedSortsStableTieBreakingAndNullGpaLast() {
        var gpaDesc = repository.searchCandidates(noFilters(), null, 0, 100, CandidateSort.OFFICIAL_GPA_DESC);
        assertThat(gpaDesc.getContent().getFirst().studentId()).isEqualTo(STUDENT_E);
        assertThat(gpaDesc.getContent().getLast().studentId()).isEqualTo(STUDENT_D);

        List<UUID> tiedDesc = gpaDesc.getContent().stream()
                .filter(row -> row.officialGpa() != null && row.officialGpa().compareTo(new BigDecimal("3.50")) == 0)
                .map(CandidateFilterRow::studentId)
                .toList();
        assertThat(tiedDesc).containsExactly(STUDENT_A, STUDENT_TIE_ONE, STUDENT_TIE_TWO);

        var gpaAsc = repository.searchCandidates(noFilters(), null, 0, 100, CandidateSort.OFFICIAL_GPA_ASC);
        assertThat(gpaAsc.getContent().getFirst().studentId()).isEqualTo(STUDENT_C);
        assertThat(gpaAsc.getContent().getLast().studentId()).isEqualTo(STUDENT_D);

        assertThat(repository.searchCandidates(noFilters(), null, 0, 100, CandidateSort.FULL_NAME_ASC)
                        .getContent())
                .extracting(CandidateFilterRow::studentId)
                .containsSubsequence(STUDENT_TIE_ONE, STUDENT_TIE_TWO);

        assertThat(ids(repository.searchCandidates(noFilters(), null, 0, 100, CandidateSort.INDEX_NUMBER_ASC)
                        .getContent()))
                .containsExactly(
                        STUDENT_A,
                        STUDENT_B,
                        STUDENT_C,
                        STUDENT_D,
                        STUDENT_E,
                        STUDENT_TIE_ONE,
                        STUDENT_TIE_TWO);
    }

    @Test
    void paginatesServerSideAndReportsSearchScopedTotals() {
        var first = repository.searchCandidates(noFilters(), null, 0, 2, CandidateSort.INDEX_NUMBER_ASC);
        var second = repository.searchCandidates(noFilters(), null, 1, 2, CandidateSort.INDEX_NUMBER_ASC);
        var beyond = repository.searchCandidates(noFilters(), null, 20, 2, CandidateSort.INDEX_NUMBER_ASC);

        assertThat(ids(first.getContent())).containsExactly(STUDENT_A, STUDENT_B);
        assertThat(ids(second.getContent())).containsExactly(STUDENT_C, STUDENT_D);
        assertThat(first.getTotalElements()).isEqualTo(7);
        assertThat(first.getTotalPages()).isEqualTo(4);
        assertThat(beyond.getContent()).isEmpty();
        assertThat(beyond.getTotalElements()).isEqualTo(7);

        var searchPage = repository.searchCandidates(noFilters(), "same name", 0, 20, CandidateSort.FULL_NAME_ASC);
        assertThat(searchPage.getTotalElements()).isEqualTo(2);
        assertThat(ids(searchPage.getContent())).containsExactly(STUDENT_TIE_ONE, STUDENT_TIE_TWO);

        assertThatThrownBy(() -> repository.searchCandidates(noFilters(), null, -1, 20, CandidateSort.DEFAULT))
                .isInstanceOf(InvalidDataAccessApiUsageException.class);
        assertThatThrownBy(() -> repository.searchCandidates(noFilters(), null, 0, 0, CandidateSort.DEFAULT))
                .isInstanceOf(InvalidDataAccessApiUsageException.class);
        assertThatThrownBy(() -> repository.searchCandidates(noFilters(), null, 0, 101, CandidateSort.DEFAULT))
                .isInstanceOf(InvalidDataAccessApiUsageException.class);
    }

    @Test
    void bulkMatchingSkillReadReturnsOnlyCriteriaIntersectionWithoutNPlusOneCalls() {
        List<UUID> pageStudents = List.of(STUDENT_A, STUDENT_B, STUDENT_E);
        var rows = repository.findMatchingDeclaredSkills(pageStudents, List.of(REACT, SPRING_BOOT));

        assertThat(rows).hasSize(3);
        assertThat(rows.stream().filter(row -> row.studentId().equals(STUDENT_A)).toList())
                .extracting(row -> row.skillId())
                .containsExactly(REACT, SPRING_BOOT);
        assertThat(rows.stream().filter(row -> row.studentId().equals(STUDENT_B)).toList())
                .singleElement()
                .satisfies(row -> {
                    assertThat(row.skillId()).isEqualTo(REACT);
                    assertThat(row.skillName()).isEqualTo("React");
                    assertThat(row.competencyLevel().name()).isEqualTo("BEGINNER");
                    assertThat(row.version()).isZero();
                    assertThat(row.createdAt()).isNotNull();
                    assertThat(row.updatedAt()).isNotNull();
                });
        assertThat(rows.stream().filter(row -> row.studentId().equals(STUDENT_E)).toList()).isEmpty();
        assertThat(repository.findMatchingDeclaredSkills(List.of(), List.of(REACT))).isEmpty();
        assertThat(repository.findMatchingDeclaredSkills(pageStudents, List.of())).isEmpty();

        assertThat(repository.searchCandidates(noFilters(), null, 0, 100, CandidateSort.INDEX_NUMBER_ASC)
                        .getContent())
                .filteredOn(row -> row.studentId().equals(STUDENT_A))
                .singleElement()
                .satisfies(row -> assertThat(row.declaredSkillCount()).isEqualTo(3));
    }

    @Test
    void sameCriteriaReflectLatestCommittedGpaAndDeclaredSkills() {
        CandidateFilteringCriteria criteria = criteria(
                "3.00", null, List.of(REACT, SPRING_BOOT), List.of(), FilterSkillMatchMode.AND);

        assertThat(ids(repository.searchCandidates(criteria, null, 0, 100, CandidateSort.INDEX_NUMBER_ASC)
                        .getContent()))
                .doesNotContain(STUDENT_C);

        jdbc.update(
                "UPDATE academic.student_academic_summary SET computer_science_gpa = 3.10 WHERE student_id = ?",
                STUDENT_C);
        jdbc.update(
                "INSERT INTO public.student_declared_skills (student_id, skill_id, competency_level) VALUES (?, ?, 'INTERMEDIATE')",
                STUDENT_C,
                REACT);

        assertThat(ids(repository.searchCandidates(criteria, null, 0, 100, CandidateSort.INDEX_NUMBER_ASC)
                        .getContent()))
                .contains(STUDENT_C);
    }

    @Test
    void patchOneQuerySupportIndexesRemainAvailableForFilteringWorkload() {
        Set<String> indexNames = Set.copyOf(jdbc.queryForList(
                """
                SELECT indexname
                FROM pg_indexes
                WHERE schemaname IN ('public', 'academic')
                  AND indexname IN (
                      'idx_student_declared_skills_skill_student',
                      'idx_student_academic_summary_gpa_student'
                  )
                """,
                String.class));

        assertThat(indexNames)
                .containsExactlyInAnyOrder(
                        "idx_student_declared_skills_skill_student",
                        "idx_student_academic_summary_gpa_student");
    }

    private CandidateFilteringCriteria noFilters() {
        return criteria(null, null, List.of(), List.of(), FilterSkillMatchMode.OR);
    }

    private CandidateFilteringCriteria criteria(
            String lower,
            String upper,
            List<UUID> requestSkillIds,
            List<UUID> additionalSkillIds,
            FilterSkillMatchMode mode) {
        return new CandidateFilteringCriteria(
                REQUEST_ID,
                lower == null ? null : new BigDecimal(lower),
                upper == null ? null : new BigDecimal(upper),
                requestSkillIds,
                additionalSkillIds,
                mode);
    }

    private List<UUID> ids(List<CandidateFilterRow> rows) {
        return rows.stream().map(CandidateFilterRow::studentId).toList();
    }

    private void seedAdminAndAcademicSource() {
        jdbc.update(
                "INSERT INTO public.user_accounts (id, university_email, account_status) VALUES (?, ?, 'ACTIVE')",
                ADMIN_ACCOUNT,
                "filtering.read.admin@dcs.ruh.ac.lk");
        jdbc.update(
                """
                INSERT INTO system.file_asset (
                    file_asset_id, owner_account_id, file_name, storage_key,
                    mime_type, file_size_bytes, checksum_sha256
                ) VALUES (?, ?, 'filtering-read.csv', 'academic-ledger/filtering-read.csv',
                          'text/csv', 32, ?)
                """,
                FILE_ASSET,
                ADMIN_ACCOUNT,
                "a".repeat(64));
        jdbc.update(
                """
                INSERT INTO academic.academic_ledger_upload (
                    academic_ledger_upload_id, uploaded_by_account_id, source_file_asset_id,
                    file_name, file_hash, upload_status, validation_status, committed_at
                ) VALUES (?, ?, ?, 'filtering-read.csv', ?, 'COMMITTED', 'PASSED', '2026-08-17T04:00:00Z')
                """,
                LEDGER_UPLOAD,
                ADMIN_ACCOUNT,
                FILE_ASSET,
                "b".repeat(64));
    }

    private void seedRequestContext() {
        jdbc.update(
                "INSERT INTO public.companies (id, name) VALUES (?, ?)",
                COMPANY_ID,
                "Deterministic Systems Ltd");
        jdbc.update(
                """
                INSERT INTO public.internship_requests (
                    id, company_id, title, shortlist_guidance_value, created_by_account_id
                ) VALUES (?, ?, 'Backend Engineering Intern', 8, ?)
                """,
                REQUEST_ID,
                COMPANY_ID,
                ADMIN_ACCOUNT);
        jdbc.update(
                "INSERT INTO public.internship_request_skills (internship_request_id, skill_id) VALUES (?, ?)",
                REQUEST_ID,
                REACT);
        jdbc.update(
                "INSERT INTO public.internship_request_skills (internship_request_id, skill_id) VALUES (?, ?)",
                REQUEST_ID,
                SPRING_BOOT);
        jdbc.update(
                """
                INSERT INTO public.skills (
                    id, skill_category_id, skill_name, skill_status, display_order
                ) VALUES (?, ?, 'Retired Filtering Skill', 'INACTIVE', 999)
                """,
                INACTIVE_SKILL,
                BACKEND_CATEGORY);
    }

    private void seedStudents() {
        for (int i = 0; i < STUDENT_ACCOUNTS.size(); i++) {
            String status = i == 6 ? "LOCKED" : "ACTIVE";
            seedAccount(STUDENT_ACCOUNTS.get(i), i + 1, status, i != 7);
        }

        seedStudent(STUDENT_A, 1, "Alpha Student", true, STUDENT_ACCOUNTS.get(0));
        seedStudent(STUDENT_B, 2, "Beta Student", true, STUDENT_ACCOUNTS.get(1));
        seedStudent(STUDENT_C, 3, "Gamma Student", true, STUDENT_ACCOUNTS.get(2));
        seedStudent(STUDENT_D, 4, "Delta Student", true, STUDENT_ACCOUNTS.get(3));
        seedStudent(STUDENT_E, 5, "Percent % Under_score", true, STUDENT_ACCOUNTS.get(4));
        seedStudent(STUDENT_INACTIVE, 6, "Inactive Student", false, STUDENT_ACCOUNTS.get(5));
        seedStudent(STUDENT_LOCKED, 7, "Locked Student", true, STUDENT_ACCOUNTS.get(6));
        seedStudent(STUDENT_NO_ROLE, 8, "No Role Student", true, STUDENT_ACCOUNTS.get(7));
        seedStudent(STUDENT_TIE_ONE, 9, "Same Name", true, STUDENT_ACCOUNTS.get(8));
        seedStudent(STUDENT_TIE_TWO, 10, "Same Name", true, STUDENT_ACCOUNTS.get(9));

        jdbc.update(
                "INSERT INTO public.student_profiles (id, student_id, display_name) VALUES (?, ?, ?)",
                UUID.fromString("85000000-0000-4000-8000-000000000001"),
                STUDENT_A,
                "Preferred Alpha");
        jdbc.update(
                "INSERT INTO public.student_profiles (id, student_id, display_name) VALUES (?, ?, ?)",
                UUID.fromString("85000000-0000-4000-8000-000000000002"),
                STUDENT_B,
                "   ");

        seedGpa(STUDENT_A, "3.50");
        seedGpa(STUDENT_B, "3.00");
        seedGpa(STUDENT_C, "2.50");
        seedGpa(STUDENT_E, "4.00");
        seedGpa(STUDENT_INACTIVE, "3.70");
        seedGpa(STUDENT_LOCKED, "3.70");
        seedGpa(STUDENT_NO_ROLE, "3.70");
        seedGpa(STUDENT_TIE_ONE, "3.50");
        seedGpa(STUDENT_TIE_TWO, "3.50");

        declare(STUDENT_A, REACT, "BEGINNER");
        declare(STUDENT_A, SPRING_BOOT, "ADVANCED");
        declare(STUDENT_A, TYPESCRIPT, "INTERMEDIATE");
        declare(STUDENT_B, REACT, "BEGINNER");
        declare(STUDENT_C, SPRING_BOOT, "INTERMEDIATE");
        declare(STUDENT_D, REACT, "ADVANCED");
        declare(STUDENT_D, SPRING_BOOT, "ADVANCED");
        declare(STUDENT_E, PYTHON, "ADVANCED");
        declare(STUDENT_INACTIVE, REACT, "ADVANCED");
        declare(STUDENT_INACTIVE, SPRING_BOOT, "ADVANCED");
        declare(STUDENT_LOCKED, REACT, "ADVANCED");
        declare(STUDENT_LOCKED, SPRING_BOOT, "ADVANCED");
        declare(STUDENT_NO_ROLE, REACT, "ADVANCED");
        declare(STUDENT_NO_ROLE, SPRING_BOOT, "ADVANCED");
        declare(STUDENT_TIE_ONE, REACT, "INTERMEDIATE");
        declare(STUDENT_TIE_ONE, SPRING_BOOT, "INTERMEDIATE");
        declare(STUDENT_TIE_TWO, REACT, "INTERMEDIATE");
        declare(STUDENT_TIE_TWO, SPRING_BOOT, "INTERMEDIATE");

        jdbc.update(
                "INSERT INTO public.student_projects (id, student_id, title) VALUES (?, ?, 'Portfolio React Project')",
                PROJECT_ID,
                STUDENT_E);
        jdbc.update(
                "INSERT INTO public.student_project_skills (project_id, skill_id) VALUES (?, ?)",
                PROJECT_ID,
                REACT);
    }

    private void seedAccount(UUID accountId, int sequence, String status, boolean studentRole) {
        jdbc.update(
                "INSERT INTO public.user_accounts (id, university_email, account_status) VALUES (?, ?, ?)",
                accountId,
                "filtering.student." + sequence + "@dcs.ruh.ac.lk",
                status);
        if (studentRole) {
            jdbc.update(
                    "INSERT INTO public.user_roles (user_id, role_id) SELECT ?, id FROM public.roles WHERE name = 'ROLE_STUDENT'",
                    accountId);
        }
    }

    private void seedStudent(UUID studentId, int sequence, String fullName, boolean active, UUID accountId) {
        jdbc.update(
                """
                INSERT INTO public.eligible_students (
                    id, index_number, university_email, full_name, academic_level, is_active, user_account_id
                ) VALUES (?, ?, ?, ?, 3, ?, ?)
                """,
                studentId,
                "SC/2022/" + String.format("%05d", 10000 + sequence),
                "filtering.student." + sequence + "@dcs.ruh.ac.lk",
                fullName,
                active,
                accountId);
    }

    private void seedGpa(UUID studentId, String gpa) {
        jdbc.update(
                """
                INSERT INTO academic.student_academic_summary (
                    student_id, computer_science_gpa, total_credits, calculated_at, source_upload_id
                ) VALUES (?, CAST(? AS NUMERIC), 120.0, '2026-08-17T04:00:00Z', ?)
                """,
                studentId,
                gpa,
                LEDGER_UPLOAD);
    }

    private void declare(UUID studentId, UUID skillId, String competency) {
        jdbc.update(
                "INSERT INTO public.student_declared_skills (student_id, skill_id, competency_level) VALUES (?, ?, ?)",
                studentId,
                skillId,
                competency);
    }

    private void cleanupFixtures() {
        jdbc.update("DELETE FROM public.internship_requests WHERE id = ?", REQUEST_ID);
        jdbc.update("DELETE FROM public.companies WHERE id = ?", COMPANY_ID);
        STUDENTS.forEach(studentId -> jdbc.update("DELETE FROM public.eligible_students WHERE id = ?", studentId));
        jdbc.update("DELETE FROM academic.academic_ledger_upload WHERE academic_ledger_upload_id = ?", LEDGER_UPLOAD);
        jdbc.update("DELETE FROM system.file_asset WHERE file_asset_id = ?", FILE_ASSET);
        STUDENT_ACCOUNTS.forEach(accountId -> jdbc.update("DELETE FROM public.user_accounts WHERE id = ?", accountId));
        jdbc.update("DELETE FROM public.user_accounts WHERE id = ?", ADMIN_ACCOUNT);
        jdbc.update("DELETE FROM public.skills WHERE id = ?", INACTIVE_SKILL);
    }

    private static UUID accountId(int sequence) {
        return UUID.fromString("83000000-0000-4000-8000-" + String.format("%012d", sequence));
    }
}
