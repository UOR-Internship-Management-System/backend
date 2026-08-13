package lk.ac.ruhuna.dcs.cvmanagement.modules.auth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.UUID;
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
class AuthApiIntegrationTest {

    private static final String OTP = "123456";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Test
    void studentCanCompleteOnboardingLoginMeAndLogout() throws Exception {
        UUID verificationId = startStudentVerification();
        setOtp(verificationId, OTP);

        mockMvc.perform(post("/api/v1/student-verifications/{id}/otp/verify", verificationId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"otpCode\":\"" + OTP + "\"}"))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/v1/student-verifications/{id}/password", verificationId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"newPassword":"StudentPass123","confirmPassword":"StudentPass123"}
                                """))
                .andExpect(status().isNoContent());

        assertThat(jdbcTemplate.queryForObject(
                "SELECT otp_hash FROM verification_sessions WHERE id = ?",
                String.class,
                verificationId)).isNotEqualTo(OTP);
        assertThat(jdbcTemplate.queryForObject(
                "SELECT account_status FROM user_accounts WHERE university_email = ?",
                String.class,
                "sc2020001@dcs.ruh.ac.lk")).isEqualTo("ACTIVE");

        String token = loginStudent("sc2020001@dcs.ruh.ac.lk", "StudentPass123");
        mockMvc.perform(get("/api/v1/auth/me").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());
        mockMvc.perform(post("/api/v1/auth/logout").header("Authorization", "Bearer " + token))
                .andExpect(status().isNoContent());
    }

    @Test
    void predefinedAdminCanLoginAndUseMe() throws Exception {
        seedAdmin("AdminPass123", true);

        String token = loginAdmin("admin@dcs.ruh.ac.lk", "AdminPass123");
        MvcResult result = mockMvc.perform(get("/api/v1/auth/me").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode body = objectMapper.readTree(result.getResponse().getContentAsString());
        assertThat(body.get("primaryRole").asText()).isEqualTo("ADMIN");
    }

    @Test
    void studentVerificationOtpResendReturnsAccepted() throws Exception {
        UUID verificationId = startStudentVerification();
        makeResendAvailable(verificationId);

        MvcResult result = mockMvc.perform(post("/api/v1/student-verifications/{id}/otp/resend", verificationId))
                .andExpect(status().isAccepted())
                .andReturn();

        JsonNode body = objectMapper.readTree(result.getResponse().getContentAsString());
        assertThat(body.get("message").asText()).isNotBlank();
        assertThat(body.get("expiresInSeconds").asLong()).isGreaterThan(0);
        assertThat(body.has("resendAvailableInSeconds")).isFalse();
    }

    @Test
    void adminPasswordResetUpdatesHashAndAllowsNewLogin() throws Exception {
        UUID adminId = seedAdmin("AdminPass123", true);

        MvcResult start = mockMvc.perform(post("/api/v1/password-resets")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"accountType":"ADMIN","email":"admin@dcs.ruh.ac.lk"}
                                """))
                .andExpect(status().isAccepted())
                .andReturn();
        UUID resetId = UUID.fromString(objectMapper.readTree(start.getResponse().getContentAsString())
                .get("resetId").asText());
        setOtp(resetId, OTP);

        mockMvc.perform(post("/api/v1/password-resets/{id}/otp/verify", resetId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"otpCode\":\"" + OTP + "\"}"))
                .andExpect(status().isOk());
        mockMvc.perform(post("/api/v1/password-resets/{id}/password", resetId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"newPassword":"NewAdminPass123","confirmPassword":"NewAdminPass123"}
                                """))
                .andExpect(status().isNoContent());

        String hash = jdbcTemplate.queryForObject(
                "SELECT password_hash FROM user_accounts WHERE id = ?",
                String.class,
                adminId);
        assertThat(passwordEncoder.matches("NewAdminPass123", hash)).isTrue();
        assertThat(loginAdmin("admin@dcs.ruh.ac.lk", "NewAdminPass123")).isNotBlank();
    }

    @Test
    void passwordResetOtpResendReturnsAccepted() throws Exception {
        seedAdmin("AdminPass123", true);

        MvcResult start = mockMvc.perform(post("/api/v1/password-resets")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"accountType":"ADMIN","email":"admin@dcs.ruh.ac.lk"}
                                """))
                .andExpect(status().isAccepted())
                .andReturn();
        UUID resetId = UUID.fromString(objectMapper.readTree(start.getResponse().getContentAsString())
                .get("resetId").asText());
        makeResendAvailable(resetId);

        MvcResult result = mockMvc.perform(post("/api/v1/password-resets/{id}/otp/resend", resetId))
                .andExpect(status().isAccepted())
                .andReturn();

        JsonNode body = objectMapper.readTree(result.getResponse().getContentAsString());
        assertThat(body.get("message").asText()).isNotBlank();
        assertThat(body.get("expiresInSeconds").asLong()).isGreaterThan(0);
        assertThat(body.has("resendAvailableInSeconds")).isFalse();
    }

    @Test
    void unknownPasswordResetEmailDoesNotCreateAccount() throws Exception {
        MvcResult start = mockMvc.perform(post("/api/v1/password-resets")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"accountType":"ADMIN","email":"missing@dcs.ruh.ac.lk"}
                                """))
                .andExpect(status().isAccepted())
                .andReturn();

        JsonNode body = objectMapper.readTree(start.getResponse().getContentAsString());
        assertThat(body.get("resetId").asText()).isNotBlank();
        assertThat(body.get("expiresInSeconds").asLong()).isGreaterThan(0);

        Integer accounts = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM user_accounts WHERE university_email = ?",
                Integer.class,
                "missing@dcs.ruh.ac.lk");
        assertThat(accounts).isZero();
    }

    @Test
    void studentAccountCannotUseAdminLogin() throws Exception {
        UUID verificationId = startStudentVerification();
        setOtp(verificationId, OTP);
        mockMvc.perform(post("/api/v1/student-verifications/{id}/otp/verify", verificationId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"otpCode\":\"" + OTP + "\"}"))
                .andExpect(status().isOk());
        mockMvc.perform(post("/api/v1/student-verifications/{id}/password", verificationId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"newPassword":"StudentPass123","confirmPassword":"StudentPass123"}
                                """))
                .andExpect(status().isNoContent());

        mockMvc.perform(post("/api/v1/auth/admin/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"sc2020001@dcs.ruh.ac.lk","password":"StudentPass123"}
                                """))
                .andExpect(status().isUnauthorized());
    }

    private UUID startStudentVerification() throws Exception {
        MvcResult result = mockMvc.perform(post("/api/v1/student-verifications")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "fullName":"Display Name",
                                  "indexNumber":"SC/2020/00001",
                                  "universityEmail":"sc2020001@dcs.ruh.ac.lk"
                                }
                                """))
                .andExpect(status().isCreated())
                .andReturn();
        return UUID.fromString(objectMapper.readTree(result.getResponse().getContentAsString())
                .get("verificationId").asText());
    }

    private String loginStudent(String email, String password) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/v1/auth/student/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"%s","password":"%s"}
                                """.formatted(email, password)))
                .andExpect(status().isOk())
                .andReturn();
        return objectMapper.readTree(result.getResponse().getContentAsString()).get("accessToken").asText();
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

    private UUID seedAdmin(String password, boolean active) {
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
                "INSERT INTO admin_users (id, user_account_id, display_name, is_active) VALUES (?, ?, ?, ?)",
                UUID.fromString("30000000-0000-0000-0000-000000000101"),
                accountId,
                "Department Administrator",
                active);
        return accountId;
    }

    private void setOtp(UUID contextId, String otp) {
        jdbcTemplate.update(
                "UPDATE verification_sessions SET otp_hash = ?, expires_at = ?, status = 'PENDING' WHERE id = ?",
                passwordEncoder.encode(otp),
                Timestamp.from(Instant.now().plusSeconds(300)),
                contextId);
    }

    private void makeResendAvailable(UUID contextId) {
        Instant oldEnough = Instant.now().minusSeconds(120);
        jdbcTemplate.update(
                "UPDATE verification_sessions SET created_at = ?, last_resend_at = NULL WHERE id = ?",
                Timestamp.from(oldEnough),
                contextId);
    }
}
