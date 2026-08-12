package lk.ac.ruhuna.dcs.cvmanagement.modules.admindashboard;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.UUID;
import lk.ac.ruhuna.dcs.cvmanagement.support.WithMockAdmin;
import lk.ac.ruhuna.dcs.cvmanagement.support.WithMockStudent;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Sql(scripts = "/sql/auth-test-schema.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
class AdminDashboardApiIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Test
    void realAdminLoginJwtCanReadLiveDashboardMetrics() throws Exception {
        seedAdmin("AdminPass123");
        seedRegisteredStudentAccount();
        jdbcTemplate.update(
                "INSERT INTO eligible_students (id, index_number, university_email, full_name, academic_level, is_active) "
                        + "VALUES (?, ?, ?, ?, ?, TRUE)",
                UUID.fromString("20000000-0000-0000-0000-000000000002"),
                "SC/2020/00002",
                "sc2020002@dcs.ruh.ac.lk",
                "Ayesha Fernando",
                3);
        jdbcTemplate.execute("CREATE TABLE internship_requests (id UUID PRIMARY KEY)");
        jdbcTemplate.update("INSERT INTO internship_requests (id) VALUES (?)",
                UUID.fromString("40000000-0000-0000-0000-000000000001"));
        jdbcTemplate.update("INSERT INTO internship_requests (id) VALUES (?)",
                UUID.fromString("40000000-0000-0000-0000-000000000002"));

        String token = loginAdmin("admin@dcs.ruh.ac.lk", "AdminPass123");
        assertThat(token).isNotBlank();

        mockMvc.perform(get("/api/v1/auth/me")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.primaryRole").value("ADMIN"))
                .andExpect(jsonPath("$.roles").isArray())
                .andExpect(jsonPath("$.roles[0]").value("ADMIN"));

        MvcResult result = mockMvc.perform(get("/api/v1/admin/dashboard/metrics")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalStudents").value(2))
                .andExpect(jsonPath("$.registeredStudents").value(1))
                .andExpect(jsonPath("$.internshipRequestsCreated").value(2))
                .andExpect(jsonPath("$.lastUpdatedAt").isString())
                .andReturn();

        JsonNode body = objectMapper.readTree(result.getResponse().getContentAsString());
        assertThat(Instant.parse(body.get("lastUpdatedAt").asText())).isNotNull();
    }

    @Test
    @WithMockAdmin
    void dashboardReturnsZeroInternshipRequestsBeforeInternshipPersistenceIsIntroduced() throws Exception {
        mockMvc.perform(get("/api/v1/admin/dashboard/metrics"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalStudents").value(1))
                .andExpect(jsonPath("$.registeredStudents").value(0))
                .andExpect(jsonPath("$.internshipRequestsCreated").value(0));
    }

    @Test
    @WithMockStudent
    void studentCannotReadAdminDashboardMetrics() throws Exception {
        mockMvc.perform(get("/api/v1/admin/dashboard/metrics"))
                .andExpect(status().isForbidden());
    }

    @Test
    void anonymousUserCannotReadAdminDashboardMetrics() throws Exception {
        mockMvc.perform(get("/api/v1/admin/dashboard/metrics"))
                .andExpect(status().isForbidden());
    }

    private String loginAdmin(String email, String password) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/v1/auth/admin/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"%s","password":"%s"}
                                """.formatted(email, password)))
                .andExpect(status().isOk())
                .andReturn();
        return objectMapper.readTree(result.getResponse().getContentAsString()).get("accessToken").asText();
    }

    private void seedAdmin(String password) {
        UUID accountId = UUID.fromString("30000000-0000-0000-0000-000000000001");
        jdbcTemplate.update(
                """
                INSERT INTO user_accounts (
                    id, university_email, password_hash, account_status, password_changed_at
                )
                VALUES (?, ?, ?, 'ACTIVE', ?)
                """,
                accountId,
                "admin@dcs.ruh.ac.lk",
                passwordEncoder.encode(password),
                Timestamp.from(Instant.now()));
        jdbcTemplate.update(
                "INSERT INTO user_roles (user_id, role_id) SELECT ?, id FROM roles WHERE name = 'ROLE_ADMIN'",
                accountId);
        jdbcTemplate.update(
                "INSERT INTO admin_users (id, user_account_id, display_name, is_active) VALUES (?, ?, ?, TRUE)",
                UUID.fromString("30000000-0000-0000-0000-000000000101"),
                accountId,
                "Department Administrator");
    }

    private void seedRegisteredStudentAccount() {
        UUID accountId = UUID.fromString("30000000-0000-0000-0000-000000000002");
        jdbcTemplate.update(
                "INSERT INTO user_accounts (id, university_email, password_hash, account_status) VALUES (?, ?, ?, 'ACTIVE')",
                accountId,
                "sc2020001@dcs.ruh.ac.lk",
                passwordEncoder.encode("StudentPass123"));
        jdbcTemplate.update(
                "INSERT INTO user_roles (user_id, role_id) SELECT ?, id FROM roles WHERE name = 'ROLE_STUDENT'",
                accountId);
        jdbcTemplate.update(
                "UPDATE eligible_students SET user_account_id = ? WHERE index_number = 'SC/2020/00001'",
                accountId);
    }
}
