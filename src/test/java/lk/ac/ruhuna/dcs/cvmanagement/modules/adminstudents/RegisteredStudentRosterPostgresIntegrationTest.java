package lk.ac.ruhuna.dcs.cvmanagement.modules.adminstudents;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.nullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import lk.ac.ruhuna.dcs.cvmanagement.modules.admindashboard.persistence.AdminDashboardMetricsQuery;
import lk.ac.ruhuna.dcs.cvmanagement.modules.adminstudents.domain.model.RegisteredStudentSort;
import lk.ac.ruhuna.dcs.cvmanagement.modules.adminstudents.persistence.query.RegisteredStudentReadRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

/** PostgreSQL and HTTP contract coverage for the registered-Student Admin roster. */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Testcontainers(disabledWithoutDocker = true)
class RegisteredStudentRosterPostgresIntegrationTest {

    private static final UUID ADMIN_ACCOUNT = UUID.fromString("71000000-0000-4000-8000-000000000001");
    private static final UUID ADMIN_USER = UUID.fromString("71000000-0000-4000-8000-000000000002");
    private static final UUID FILE_ASSET = UUID.fromString("71000000-0000-4000-8000-000000000003");
    private static final UUID LEDGER_UPLOAD = UUID.fromString("71000000-0000-4000-8000-000000000004");

    private static final UUID STUDENT_ONE = UUID.fromString("72000000-0000-4000-8000-000000000001");
    private static final UUID STUDENT_TWO = UUID.fromString("72000000-0000-4000-8000-000000000002");
    private static final UUID STUDENT_SPECIAL = UUID.fromString("72000000-0000-4000-8000-000000000003");
    private static final UUID UNREGISTERED_STUDENT = UUID.fromString("72000000-0000-4000-8000-000000000004");
    private static final UUID INACTIVE_STUDENT = UUID.fromString("72000000-0000-4000-8000-000000000005");
    private static final UUID LOCKED_STUDENT = UUID.fromString("72000000-0000-4000-8000-000000000006");
    private static final UUID NO_ROLE_STUDENT = UUID.fromString("72000000-0000-4000-8000-000000000007");

    private static final UUID STUDENT_ONE_ACCOUNT = UUID.fromString("73000000-0000-4000-8000-000000000001");
    private static final UUID STUDENT_TWO_ACCOUNT = UUID.fromString("73000000-0000-4000-8000-000000000002");
    private static final UUID STUDENT_SPECIAL_ACCOUNT = UUID.fromString("73000000-0000-4000-8000-000000000003");
    private static final UUID INACTIVE_ACCOUNT = UUID.fromString("73000000-0000-4000-8000-000000000005");
    private static final UUID LOCKED_ACCOUNT = UUID.fromString("73000000-0000-4000-8000-000000000006");
    private static final UUID NO_ROLE_ACCOUNT = UUID.fromString("73000000-0000-4000-8000-000000000007");

    private static final Set<UUID> TEST_STUDENT_IDS = Set.of(
            STUDENT_ONE,
            STUDENT_TWO,
            STUDENT_SPECIAL,
            UNREGISTERED_STUDENT,
            INACTIVE_STUDENT,
            LOCKED_STUDENT,
            NO_ROLE_STUDENT);

    @Container
    private static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:16-alpine")
            .withDatabaseName("cv_management_admin_students_roster_test")
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

    @Autowired private JdbcTemplate jdbcTemplate;
    @Autowired private RegisteredStudentReadRepository repository;
    @Autowired private AdminDashboardMetricsQuery dashboardMetricsQuery;
    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @Autowired private PasswordEncoder passwordEncoder;

    @BeforeEach
    void seedRoster() {
        cleanupFixtures();
        seedAdmin();
        seedAcademicSource();

        seedAccount(STUDENT_ONE_ACCOUNT, "sc202210001@dcs.ruh.ac.lk", "ACTIVE", true, "StudentPass123");
        seedAccount(STUDENT_TWO_ACCOUNT, "sc202110002@dcs.ruh.ac.lk", "ACTIVE", true, "StudentPass123");
        seedAccount(STUDENT_SPECIAL_ACCOUNT, "sc202210003@dcs.ruh.ac.lk", "ACTIVE", true, "StudentPass123");
        seedAccount(INACTIVE_ACCOUNT, "sc202210005@dcs.ruh.ac.lk", "ACTIVE", true, "StudentPass123");
        seedAccount(LOCKED_ACCOUNT, "sc202210006@dcs.ruh.ac.lk", "LOCKED", true, "StudentPass123");
        seedAccount(NO_ROLE_ACCOUNT, "sc202210007@dcs.ruh.ac.lk", "ACTIVE", false, "StudentPass123");

        seedStudent(STUDENT_ONE, "SC/2022/10001", "sc202210001@dcs.ruh.ac.lk", "Fallback One", 3, true, STUDENT_ONE_ACCOUNT);
        seedStudent(STUDENT_TWO, "SC/2021/10002", "sc202110002@dcs.ruh.ac.lk", "Fallback Two", 4, true, STUDENT_TWO_ACCOUNT);
        seedStudent(STUDENT_SPECIAL, "SC/2022/10003", "sc202210003@dcs.ruh.ac.lk", "Percent % Under_score", 4, true, STUDENT_SPECIAL_ACCOUNT);
        seedStudent(UNREGISTERED_STUDENT, "SC/2020/10004", "sc202010004@dcs.ruh.ac.lk", "Eligible Only", 3, true, null);
        seedStudent(INACTIVE_STUDENT, "SC/2022/10005", "sc202210005@dcs.ruh.ac.lk", "Inactive Student", 3, false, INACTIVE_ACCOUNT);
        seedStudent(LOCKED_STUDENT, "SC/2022/10006", "sc202210006@dcs.ruh.ac.lk", "Locked Student", 3, true, LOCKED_ACCOUNT);
        seedStudent(NO_ROLE_STUDENT, "SC/2022/10007", "sc202210007@dcs.ruh.ac.lk", "No Role Student", 3, true, NO_ROLE_ACCOUNT);

        jdbcTemplate.update(
                "INSERT INTO public.student_profiles (id, student_id, display_name) VALUES (?, ?, ?)",
                UUID.fromString("74000000-0000-4000-8000-000000000001"),
                STUDENT_ONE,
                "Preferred One");
        jdbcTemplate.update(
                "INSERT INTO public.student_profiles (id, student_id, display_name) VALUES (?, ?, ?)",
                UUID.fromString("74000000-0000-4000-8000-000000000002"),
                STUDENT_TWO,
                "   ");

        seedAcademicSummary(STUDENT_ONE, "3.70");
        seedAcademicSummary(STUDENT_SPECIAL, "3.20");
    }

    @Test
    void rosterUsesTheDashboardRegisteredStudentPredicate() {
        var page = repository.search(null, null, 0, 20, RegisteredStudentSort.FULL_NAME_ASC);

        assertThat(page.getTotalElements()).isEqualTo(3);
        assertThat(page.getContent().stream().map(row -> row.studentId()).collect(Collectors.toSet()))
                .containsExactlyInAnyOrder(STUDENT_ONE, STUDENT_TWO, STUDENT_SPECIAL);
        assertThat(dashboardMetricsQuery.countRegisteredStudents()).isEqualTo(page.getTotalElements());
    }

    @Test
    void rosterSupportsResolvedNameIndexEmailBatchAndLevelSearch() {
        assertThat(repository.search("preferred", null, 0, 20, RegisteredStudentSort.FULL_NAME_ASC)
                        .getContent())
                .singleElement()
                .satisfies(row -> assertThat(row.studentId()).isEqualTo(STUDENT_ONE));

        assertThat(repository.search("sc/2021/10002", null, 0, 20, RegisteredStudentSort.FULL_NAME_ASC)
                        .getContent())
                .singleElement()
                .satisfies(row -> {
                    assertThat(row.studentId()).isEqualTo(STUDENT_TWO);
                    assertThat(row.fullName()).isEqualTo("Fallback Two");
                });

        assertThat(repository.search("sc202210003@dcs.ruh.ac.lk", null, 0, 20, RegisteredStudentSort.FULL_NAME_ASC)
                        .getContent())
                .singleElement()
                .satisfies(row -> assertThat(row.studentId()).isEqualTo(STUDENT_SPECIAL));

        assertThat(repository.search("2022", null, 0, 20, RegisteredStudentSort.FULL_NAME_ASC).getTotalElements())
                .isEqualTo(2);
        assertThat(repository.search(null, 4, 0, 20, RegisteredStudentSort.FULL_NAME_ASC).getTotalElements())
                .isEqualTo(2);
    }

    @Test
    void rosterTreatsPercentUnderscoreAndBackslashAsLiteralSearchCharacters() {
        assertThat(repository.search("%", null, 0, 20, RegisteredStudentSort.FULL_NAME_ASC).getContent())
                .singleElement()
                .satisfies(row -> assertThat(row.studentId()).isEqualTo(STUDENT_SPECIAL));
        assertThat(repository.search("_", null, 0, 20, RegisteredStudentSort.FULL_NAME_ASC).getContent())
                .singleElement()
                .satisfies(row -> assertThat(row.studentId()).isEqualTo(STUDENT_SPECIAL));
        assertThat(repository.search("\\", null, 0, 20, RegisteredStudentSort.FULL_NAME_ASC).getContent())
                .isEmpty();
    }

    @Test
    void rosterKeepsNullGpaLastForBothGpaDirectionsAndUsesStablePaging() {
        var descending = repository.search(null, null, 0, 20, RegisteredStudentSort.GPA_DESC).getContent();
        assertThat(descending).extracting(row -> row.officialGpa())
                .containsExactly(new BigDecimal("3.70"), new BigDecimal("3.20"), null);

        var ascending = repository.search(null, null, 0, 20, RegisteredStudentSort.GPA_ASC).getContent();
        assertThat(ascending).extracting(row -> row.officialGpa())
                .containsExactly(new BigDecimal("3.20"), new BigDecimal("3.70"), null);

        var firstPage = repository.search(null, null, 0, 1, RegisteredStudentSort.INDEX_NUMBER_ASC);
        var secondPage = repository.search(null, null, 1, 1, RegisteredStudentSort.INDEX_NUMBER_ASC);
        assertThat(firstPage.getTotalElements()).isEqualTo(3);
        assertThat(firstPage.getTotalPages()).isEqualTo(3);
        assertThat(firstPage.getContent().getFirst().studentId())
                .isNotEqualTo(secondPage.getContent().getFirst().studentId());
    }

    @Test
    void adminEndpointReturnsTheFrontendRosterContract() throws Exception {
        String token = login("/api/v1/auth/admin/login", "roster.admin@dcs.ruh.ac.lk", "AdminPass123");

        mockMvc.perform(get("/api/v1/admin/students")
                        .header("Authorization", "Bearer " + token)
                        .param("page", "0")
                        .param("size", "20")
                        .param("sort", "officialGpa,desc")
                        .param("search", "")
                        .param("level", "4"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.items.length()").value(2))
                .andExpect(jsonPath("$.items[0].studentId").value(STUDENT_SPECIAL.toString()))
                .andExpect(jsonPath("$.items[0].indexNumber").value("SC/2022/10003"))
                .andExpect(jsonPath("$.items[0].fullName").value("Percent % Under_score"))
                .andExpect(jsonPath("$.items[0].universityEmail").value("sc202210003@dcs.ruh.ac.lk"))
                .andExpect(jsonPath("$.items[0].degreeProgram").value("BSc Honours in Computer Science"))
                .andExpect(jsonPath("$.items[0].academicBatch").value("2022"))
                .andExpect(jsonPath("$.items[0].currentLevel").value(4))
                .andExpect(jsonPath("$.items[0].officialGpa").value(3.20))
                .andExpect(jsonPath("$.items[1].officialGpa").value(nullValue()))
                .andExpect(jsonPath("$.page.page").value(0))
                .andExpect(jsonPath("$.page.size").value(20))
                .andExpect(jsonPath("$.page.totalElements").value(2))
                .andExpect(jsonPath("$.page.totalPages").value(1))
                .andExpect(jsonPath("$.page.sort").value("officialGpa,desc"));
    }

    @Test
    void rosterEndpointEnforcesAdminSecurityAndCanonicalValidationErrors() throws Exception {
        mockMvc.perform(get("/api/v1/admin/students"))
                .andExpect(status().isUnauthorized())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.code").value("UNAUTHORIZED"));

        String studentToken = login("/api/v1/auth/student/login", "sc202210001@dcs.ruh.ac.lk", "StudentPass123");
        mockMvc.perform(get("/api/v1/admin/students")
                        .header("Authorization", "Bearer " + studentToken))
                .andExpect(status().isForbidden())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.code").value("FORBIDDEN"));

        String adminToken = login("/api/v1/auth/admin/login", "roster.admin@dcs.ruh.ac.lk", "AdminPass123");
        mockMvc.perform(get("/api/v1/admin/students")
                        .header("Authorization", "Bearer " + adminToken)
                        .header("X-Correlation-Id", "roster-validation-test")
                        .param("size", "101"))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(header().string("X-Correlation-Id", "roster-validation-test"))
                .andExpect(jsonPath("$.code").value("BAD_REQUEST"))
                .andExpect(jsonPath("$.correlationId").value("roster-validation-test"));

        mockMvc.perform(get("/api/v1/admin/students")
                        .header("Authorization", "Bearer " + adminToken)
                        .param("sort", "fullName,desc"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("BAD_REQUEST"));

        mockMvc.perform(get("/api/v1/admin/students")
                        .header("Authorization", "Bearer " + adminToken)
                        .param("level", "5"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("BAD_REQUEST"));
    }

    private String login(String path, String email, String password) throws Exception {
        MvcResult result = mockMvc.perform(post(path)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"%s","password":"%s"}
                                """.formatted(email, password)))
                .andExpect(status().isOk())
                .andReturn();
        return objectMapper.readTree(result.getResponse().getContentAsString()).get("accessToken").asText();
    }

    private void seedAdmin() {
        jdbcTemplate.update(
                """
                INSERT INTO public.user_accounts (
                    id, university_email, password_hash, account_status, password_changed_at
                ) VALUES (?, ?, ?, 'ACTIVE', ?)
                """,
                ADMIN_ACCOUNT,
                "roster.admin@dcs.ruh.ac.lk",
                passwordEncoder.encode("AdminPass123"),
                Timestamp.from(Instant.now()));
        jdbcTemplate.update(
                "INSERT INTO public.user_roles (user_id, role_id) SELECT ?, id FROM public.roles WHERE name = 'ROLE_ADMIN'",
                ADMIN_ACCOUNT);
        jdbcTemplate.update(
                "INSERT INTO public.admin_users (id, user_account_id, display_name, is_active) VALUES (?, ?, ?, TRUE)",
                ADMIN_USER,
                ADMIN_ACCOUNT,
                "Roster Test Administrator");
    }

    private void seedAcademicSource() {
        jdbcTemplate.update(
                """
                INSERT INTO system.file_asset (
                    file_asset_id, owner_account_id, file_name, storage_key, mime_type, file_size_bytes, checksum_sha256
                ) VALUES (?, ?, 'roster.csv', 'academic-ledger/roster-test.csv', 'text/csv', 32, ?)
                """,
                FILE_ASSET,
                ADMIN_ACCOUNT,
                "a".repeat(64));
        jdbcTemplate.update(
                """
                INSERT INTO academic.academic_ledger_upload (
                    academic_ledger_upload_id, uploaded_by_account_id, source_file_asset_id,
                    file_name, file_hash, upload_status, validation_status, committed_at
                ) VALUES (?, ?, ?, 'roster.csv', ?, 'COMMITTED', 'PASSED', '2026-08-14T06:00:00Z')
                """,
                LEDGER_UPLOAD,
                ADMIN_ACCOUNT,
                FILE_ASSET,
                "b".repeat(64));
    }

    private void seedAccount(UUID accountId, String email, String status, boolean studentRole, String password) {
        jdbcTemplate.update(
                "INSERT INTO public.user_accounts (id, university_email, password_hash, account_status) VALUES (?, ?, ?, ?)",
                accountId,
                email,
                passwordEncoder.encode(password),
                status);
        if (studentRole) {
            jdbcTemplate.update(
                    "INSERT INTO public.user_roles (user_id, role_id) SELECT ?, id FROM public.roles WHERE name = 'ROLE_STUDENT'",
                    accountId);
        }
    }

    private void seedStudent(
            UUID studentId,
            String indexNumber,
            String email,
            String fullName,
            int level,
            boolean active,
            UUID accountId) {
        jdbcTemplate.update(
                """
                INSERT INTO public.eligible_students (
                    id, index_number, university_email, full_name, academic_level, is_active, user_account_id
                ) VALUES (?, ?, ?, ?, ?, ?, ?)
                """,
                studentId,
                indexNumber,
                email,
                fullName,
                level,
                active,
                accountId);
    }

    private void seedAcademicSummary(UUID studentId, String gpa) {
        jdbcTemplate.update(
                """
                INSERT INTO academic.student_academic_summary (
                    student_id, computer_science_gpa, total_credits, calculated_at, source_upload_id
                ) VALUES (?, CAST(? AS NUMERIC), 120.0, '2026-08-14T06:00:00Z', ?)
                """,
                studentId,
                gpa,
                LEDGER_UPLOAD);
    }

    private void cleanupFixtures() {
        TEST_STUDENT_IDS.forEach(studentId ->
                jdbcTemplate.update("DELETE FROM public.eligible_students WHERE id = ?", studentId));
        jdbcTemplate.update("DELETE FROM academic.academic_ledger_upload WHERE academic_ledger_upload_id = ?", LEDGER_UPLOAD);
        jdbcTemplate.update("DELETE FROM system.file_asset WHERE file_asset_id = ?", FILE_ASSET);
        jdbcTemplate.update("DELETE FROM public.admin_users WHERE id = ?", ADMIN_USER);
        Set.of(
                        ADMIN_ACCOUNT,
                        STUDENT_ONE_ACCOUNT,
                        STUDENT_TWO_ACCOUNT,
                        STUDENT_SPECIAL_ACCOUNT,
                        INACTIVE_ACCOUNT,
                        LOCKED_ACCOUNT,
                        NO_ROLE_ACCOUNT)
                .forEach(accountId -> jdbcTemplate.update("DELETE FROM public.user_accounts WHERE id = ?", accountId));
    }
}
