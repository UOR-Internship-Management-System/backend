package lk.ac.ruhuna.dcs.cvmanagement.modules.adminstudents;

import static org.hamcrest.Matchers.nullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.Set;
import java.util.UUID;
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

/** PostgreSQL and HTTP contract coverage for the read-only Admin Student deep-dive. */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Testcontainers(disabledWithoutDocker = true)
class AdminStudentDeepDivePostgresIntegrationTest {

    private static final UUID ADMIN_ACCOUNT = UUID.fromString("81000000-0000-4000-8000-000000000001");
    private static final UUID ADMIN_USER = UUID.fromString("81000000-0000-4000-8000-000000000002");
    private static final UUID FULL_STUDENT = UUID.fromString("82000000-0000-4000-8000-000000000001");
    private static final UUID EMPTY_STUDENT = UUID.fromString("82000000-0000-4000-8000-000000000002");
    private static final UUID UNREGISTERED_STUDENT = UUID.fromString("82000000-0000-4000-8000-000000000003");
    private static final UUID FULL_ACCOUNT = UUID.fromString("83000000-0000-4000-8000-000000000001");
    private static final UUID EMPTY_ACCOUNT = UUID.fromString("83000000-0000-4000-8000-000000000002");
    private static final UUID PROFILE_ID = UUID.fromString("84000000-0000-4000-8000-000000000001");
    private static final UUID EXPERIENCE_ID = UUID.fromString("85000000-0000-4000-8000-000000000001");
    private static final UUID CERTIFICATE_ID = UUID.fromString("85000000-0000-4000-8000-000000000002");
    private static final UUID AWARD_ID = UUID.fromString("85000000-0000-4000-8000-000000000003");
    private static final UUID ACTIVITY_ID = UUID.fromString("85000000-0000-4000-8000-000000000004");
    private static final UUID CONTACT_LINK_ID = UUID.fromString("85000000-0000-4000-8000-000000000005");

    @Container
    private static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:16-alpine")
            .withDatabaseName("cv_management_admin_student_deep_dive_test")
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
    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @Autowired private PasswordEncoder passwordEncoder;

    @BeforeEach
    void seedDeepDive() {
        cleanupFixtures();
        seedAdmin();
        seedStudentAccount(FULL_ACCOUNT, "sc202210001@dcs.ruh.ac.lk", "StudentPass123");
        seedStudentAccount(EMPTY_ACCOUNT, "sc202310002@dcs.ruh.ac.lk", "StudentPass123");
        seedStudent(FULL_STUDENT, "SC/2022/10001", "sc202210001@dcs.ruh.ac.lk", "Identity Full", 3, FULL_ACCOUNT);
        seedStudent(EMPTY_STUDENT, "SC/2023/10002", "sc202310002@dcs.ruh.ac.lk", "Identity Empty", 4, EMPTY_ACCOUNT);
        seedStudent(
                UNREGISTERED_STUDENT,
                "SC/2021/10003",
                "sc202110003@dcs.ruh.ac.lk",
                "Eligible Only",
                3,
                null);
        seedFullProfileAndSupportingData();
    }

    @Test
    void deepDiveReturnsTheStrictFrontendProfileAndSupportingDataContract() throws Exception {
        String token = adminToken();

        mockMvc.perform(get("/api/v1/admin/students/{studentId}", FULL_STUDENT)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.student.studentId").value(FULL_STUDENT.toString()))
                .andExpect(jsonPath("$.student.fullName").value("Preferred Full"))
                .andExpect(jsonPath("$.student.officialGpa").value(nullValue()))
                .andExpect(jsonPath("$.profile.studentId").value(FULL_STUDENT.toString()))
                .andExpect(jsonPath("$.profile.fullName").value("Preferred Full"))
                .andExpect(jsonPath("$.profile.indexNumber").value("SC/2022/10001"))
                .andExpect(jsonPath("$.profile.universityEmail").value("sc202210001@dcs.ruh.ac.lk"))
                .andExpect(jsonPath("$.profile.degreeProgramme").value("BSc Honours in Computer Science"))
                .andExpect(jsonPath("$.profile.studentLevel").value(3))
                .andExpect(jsonPath("$.profile.cohortYear").value(2022))
                .andExpect(jsonPath("$.profile.personalEmail").value("asha@example.com"))
                .andExpect(jsonPath("$.profile.profilePhoto").value(nullValue()))
                .andExpect(jsonPath("$.profile.version").value(2))
                .andExpect(jsonPath("$.profile.cvSourceUpdatedAt").value("2026-08-14T14:00:00Z"))
                .andExpect(jsonPath("$.cvSupportingData.experiences.length()").value(1))
                .andExpect(jsonPath("$.cvSupportingData.experiences[0].id").value(EXPERIENCE_ID.toString()))
                .andExpect(jsonPath("$.cvSupportingData.experiences[0].positionTitle").value("Engineering Intern"))
                .andExpect(jsonPath("$.cvSupportingData.experiences[0].currentRole").value(true))
                .andExpect(jsonPath("$.cvSupportingData.experiences[0].endDate").value(nullValue()))
                .andExpect(jsonPath("$.cvSupportingData.certificates[0].id").value(CERTIFICATE_ID.toString()))
                .andExpect(jsonPath("$.cvSupportingData.certificates[0].issuer").value("Open Learning"))
                .andExpect(jsonPath("$.cvSupportingData.certificates[0].evidence").value(nullValue()))
                .andExpect(jsonPath("$.cvSupportingData.awards[0].id").value(AWARD_ID.toString()))
                .andExpect(jsonPath("$.cvSupportingData.activities[0].id").value(ACTIVITY_ID.toString()))
                .andExpect(jsonPath("$.latestCv.availability").value("NOT_SAVED"))
                .andExpect(jsonPath("$.latestCv.cvId").value(nullValue()))
                .andExpect(jsonPath("$.latestCv.downloadUrl").value(nullValue()));
    }

    @Test
    void registeredStudentWithoutProfileGetsSyntheticReadResponseAndNoProfileIsCreated() throws Exception {
        String token = adminToken();
        Integer before = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM public.student_profiles WHERE student_id = ?",
                Integer.class,
                EMPTY_STUDENT);

        mockMvc.perform(get("/api/v1/admin/students/{studentId}", EMPTY_STUDENT)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.student.fullName").value("Identity Empty"))
                .andExpect(jsonPath("$.profile.fullName").value("Identity Empty"))
                .andExpect(jsonPath("$.profile.personalEmail").value(nullValue()))
                .andExpect(jsonPath("$.profile.headline").value(nullValue()))
                .andExpect(jsonPath("$.profile.version").value(0))
                .andExpect(jsonPath("$.cvSupportingData.experiences.length()").value(0))
                .andExpect(jsonPath("$.cvSupportingData.certificates.length()").value(0))
                .andExpect(jsonPath("$.cvSupportingData.awards.length()").value(0))
                .andExpect(jsonPath("$.cvSupportingData.activities.length()").value(0));

        Integer after = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM public.student_profiles WHERE student_id = ?",
                Integer.class,
                EMPTY_STUDENT);
        org.assertj.core.api.Assertions.assertThat(before).isZero();
        org.assertj.core.api.Assertions.assertThat(after).isZero();
    }

    @Test
    void deepDiveDoesNotMutateStudentOwnedRows() throws Exception {
        String token = adminToken();
        Long profileVersionBefore = jdbcTemplate.queryForObject(
                "SELECT version FROM public.student_profiles WHERE student_id = ?",
                Long.class,
                FULL_STUDENT);
        Instant profileUpdatedBefore = jdbcTemplate.queryForObject(
                "SELECT updated_at FROM public.student_profiles WHERE student_id = ?",
                (rs, rowNum) -> rs.getTimestamp(1).toInstant(),
                FULL_STUDENT);
        Long supportCountBefore = supportingRowCount(FULL_STUDENT);

        mockMvc.perform(get("/api/v1/admin/students/{studentId}", FULL_STUDENT)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());

        Long profileVersionAfter = jdbcTemplate.queryForObject(
                "SELECT version FROM public.student_profiles WHERE student_id = ?",
                Long.class,
                FULL_STUDENT);
        Instant profileUpdatedAfter = jdbcTemplate.queryForObject(
                "SELECT updated_at FROM public.student_profiles WHERE student_id = ?",
                (rs, rowNum) -> rs.getTimestamp(1).toInstant(),
                FULL_STUDENT);
        Long supportCountAfter = supportingRowCount(FULL_STUDENT);

        org.assertj.core.api.Assertions.assertThat(profileVersionAfter).isEqualTo(profileVersionBefore);
        org.assertj.core.api.Assertions.assertThat(profileUpdatedAfter).isEqualTo(profileUpdatedBefore);
        org.assertj.core.api.Assertions.assertThat(supportCountAfter).isEqualTo(supportCountBefore);
    }

    @Test
    void unregisteredUnknownAndMalformedIdentifiersAreRejectedSafely() throws Exception {
        String token = adminToken();

        mockMvc.perform(get("/api/v1/admin/students/{studentId}", UNREGISTERED_STUDENT)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNotFound())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.code").value("REGISTERED_STUDENT_NOT_FOUND"));

        mockMvc.perform(get("/api/v1/admin/students/{studentId}", UUID.randomUUID())
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("REGISTERED_STUDENT_NOT_FOUND"));

        mockMvc.perform(get("/api/v1/admin/students/not-a-uuid")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.code").value("VALIDATION_FAILED"));
    }

    @Test
    void deepDiveRequiresAdminAuthorization() throws Exception {
        mockMvc.perform(get("/api/v1/admin/students/{studentId}", FULL_STUDENT))
                .andExpect(status().isUnauthorized());

        String studentToken = login(
                "/api/v1/auth/student/login",
                "sc202210001@dcs.ruh.ac.lk",
                "StudentPass123");
        mockMvc.perform(get("/api/v1/admin/students/{studentId}", FULL_STUDENT)
                        .header("Authorization", "Bearer " + studentToken))
                .andExpect(status().isForbidden());
    }

    private Long supportingRowCount(UUID studentId) {
        return jdbcTemplate.queryForObject(
                """
                SELECT
                    (SELECT COUNT(*) FROM public.student_work_experience WHERE student_id = ?) +
                    (SELECT COUNT(*) FROM public.student_certificates WHERE student_id = ?) +
                    (SELECT COUNT(*) FROM public.student_awards WHERE student_id = ?) +
                    (SELECT COUNT(*) FROM public.student_activities WHERE student_id = ?)
                """,
                Long.class,
                studentId,
                studentId,
                studentId,
                studentId);
    }

    private String adminToken() throws Exception {
        return login("/api/v1/auth/admin/login", "deepdive.admin@dcs.ruh.ac.lk", "AdminPass123");
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
                "deepdive.admin@dcs.ruh.ac.lk",
                passwordEncoder.encode("AdminPass123"),
                Timestamp.from(Instant.parse("2026-08-14T08:00:00Z")));
        jdbcTemplate.update(
                "INSERT INTO public.user_roles (user_id, role_id) SELECT ?, id FROM public.roles WHERE name = 'ROLE_ADMIN'",
                ADMIN_ACCOUNT);
        jdbcTemplate.update(
                "INSERT INTO public.admin_users (id, user_account_id, display_name, is_active) VALUES (?, ?, ?, TRUE)",
                ADMIN_USER,
                ADMIN_ACCOUNT,
                "Deep-Dive Test Administrator");
    }

    private void seedStudentAccount(UUID accountId, String email, String password) {
        jdbcTemplate.update(
                "INSERT INTO public.user_accounts (id, university_email, password_hash, account_status) VALUES (?, ?, ?, 'ACTIVE')",
                accountId,
                email,
                passwordEncoder.encode(password));
        jdbcTemplate.update(
                "INSERT INTO public.user_roles (user_id, role_id) SELECT ?, id FROM public.roles WHERE name = 'ROLE_STUDENT'",
                accountId);
    }

    private void seedStudent(
            UUID studentId,
            String indexNumber,
            String email,
            String fullName,
            int level,
            UUID accountId) {
        jdbcTemplate.update(
                """
                INSERT INTO public.eligible_students (
                    id, index_number, university_email, full_name, academic_level,
                    is_active, user_account_id, created_at, updated_at
                ) VALUES (?, ?, ?, ?, ?, TRUE, ?, '2026-08-14T08:00:00Z', '2026-08-14T08:00:00Z')
                """,
                studentId,
                indexNumber,
                email,
                fullName,
                level,
                accountId);
    }

    private void seedFullProfileAndSupportingData() {
        jdbcTemplate.update(
                """
                INSERT INTO public.student_profiles (
                    id, student_id, display_name, personal_email, headline, summary, phone, location,
                    version, created_at, updated_at
                ) VALUES (?, ?, 'Preferred Full', 'asha@example.com', 'Software engineering undergraduate',
                          'Interested in dependable systems.', '+94 77 123 4567', 'Matara', 2,
                          '2026-08-14T08:30:00Z', '2026-08-14T09:00:00Z')
                """,
                PROFILE_ID,
                FULL_STUDENT);
        jdbcTemplate.update(
                """
                INSERT INTO public.student_contact_links (
                    id, student_id, label, url, display_order, cv_include, version, created_at, updated_at
                ) VALUES (?, ?, 'GitHub', 'https://github.com/example', 1, TRUE, 1,
                          '2026-08-14T09:00:00Z', '2026-08-14T10:00:00Z')
                """,
                CONTACT_LINK_ID,
                FULL_STUDENT);
        jdbcTemplate.update(
                """
                INSERT INTO public.student_work_experience (
                    id, student_id, organization, position_title, start_date, end_date,
                    description, cv_include, version, location, is_current_role, created_at, updated_at
                ) VALUES (?, ?, 'Example Labs', 'Engineering Intern', '2026-01-01', NULL,
                          'Built administrative interfaces.', TRUE, 1, 'Colombo', TRUE,
                          '2026-08-14T10:00:00Z', '2026-08-14T11:00:00Z')
                """,
                EXPERIENCE_ID,
                FULL_STUDENT);
        jdbcTemplate.update(
                """
                INSERT INTO public.student_certificates (
                    id, student_id, title, issuer, issue_date, credential_url, cv_include,
                    version, created_at, updated_at
                ) VALUES (?, ?, 'Web Accessibility Foundations', 'Open Learning', '2025-10-10',
                          'https://example.com/credentials/asha', TRUE, 1,
                          '2026-08-14T11:00:00Z', '2026-08-14T12:00:00Z')
                """,
                CERTIFICATE_ID,
                FULL_STUDENT);
        jdbcTemplate.update(
                """
                INSERT INTO public.student_awards (
                    id, student_id, title, issuer, award_date, description, cv_include,
                    version, created_at, updated_at
                ) VALUES (?, ?, 'Faculty Project Award', 'University of Ruhuna', '2025-11-15',
                          'Recognized for a dependable design.', TRUE, 1,
                          '2026-08-14T12:00:00Z', '2026-08-14T13:00:00Z')
                """,
                AWARD_ID,
                FULL_STUDENT);
        jdbcTemplate.update(
                """
                INSERT INTO public.student_activities (
                    id, student_id, activity_name, role_title, start_date, end_date, description,
                    cv_include, version, created_at, updated_at
                ) VALUES (?, ?, 'Computer Science Society', 'Committee Member', '2024-01-01', '2025-12-31',
                          'Supported technical learning sessions.', TRUE, 1,
                          '2026-08-14T13:00:00Z', '2026-08-14T14:00:00Z')
                """,
                ACTIVITY_ID,
                FULL_STUDENT);
    }

    private void cleanupFixtures() {
        Set.of(FULL_STUDENT, EMPTY_STUDENT, UNREGISTERED_STUDENT)
                .forEach(studentId -> jdbcTemplate.update("DELETE FROM public.eligible_students WHERE id = ?", studentId));
        jdbcTemplate.update("DELETE FROM public.admin_users WHERE id = ?", ADMIN_USER);
        Set.of(ADMIN_ACCOUNT, FULL_ACCOUNT, EMPTY_ACCOUNT)
                .forEach(accountId -> jdbcTemplate.update("DELETE FROM public.user_accounts WHERE id = ?", accountId));
    }
}
