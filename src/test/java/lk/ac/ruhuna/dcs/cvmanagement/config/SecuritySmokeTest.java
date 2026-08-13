package lk.ac.ruhuna.dcs.cvmanagement.config;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class SecuritySmokeTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void healthEndpointIsPublic() throws Exception {
        mockMvc.perform(get("/api/v1/health"))
                .andExpect(status().isOk());
    }

    @Test
    void protectedStudentPatternIsNotPublic() throws Exception {
        mockMvc.perform(get("/api/v1/student/profile"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void protectedAdminPatternIsNotPublic() throws Exception {
        mockMvc.perform(get("/api/v1/admin/dashboard"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void studentVerificationPathIsNotBlocked() throws Exception {
        // Controller does not exist yet, so 404 or 405 is acceptable.
        // 401 or 403 would indicate a security misconfiguration.
        mockMvc.perform(post("/api/v1/student-verifications"))
                .andExpect(result -> {
                    int status = result.getResponse().getStatus();
                    if (status == 401 || status == 403) {
                        throw new AssertionError(
                                "Public path /api/v1/student-verifications returned " + status
                                        + "; expected 404 or 405 (no controller yet), not a security block.");
                    }
                });
    }

    @Test
    void studentVerificationSubPathIsNotBlocked() throws Exception {
        mockMvc.perform(post("/api/v1/student-verifications/00000000-0000-0000-0000-000000000001/otp/verify"))
                .andExpect(result -> {
                    int status = result.getResponse().getStatus();
                    if (status == 401 || status == 403) {
                        throw new AssertionError(
                                "Public path /api/v1/student-verifications/.../otp/verify returned " + status
                                        + "; expected 404 or 405, not a security block.");
                    }
                });
    }

    @Test
    void passwordResetPathIsNotBlocked() throws Exception {
        mockMvc.perform(post("/api/v1/password-resets"))
                .andExpect(result -> {
                    int status = result.getResponse().getStatus();
                    if (status == 401 || status == 403) {
                        throw new AssertionError(
                                "Public path /api/v1/password-resets returned " + status
                                        + "; expected 404 or 405, not a security block.");
                    }
                });
    }

    @Test
    void authPathIsNotBlocked() throws Exception {
        mockMvc.perform(post("/api/v1/auth/student/login"))
                .andExpect(result -> {
                    int status = result.getResponse().getStatus();
                    if (status == 401 || status == 403) {
                        throw new AssertionError(
                                "Public path /api/v1/auth/student/login returned " + status
                                        + "; expected 404 or 405, not a security block.");
                    }
                });
    }
}
