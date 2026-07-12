package lk.ac.ruhuna.dcs.cvmanagement.modules.auth.api;

import jakarta.validation.Valid;
import lk.ac.ruhuna.dcs.cvmanagement.modules.auth.api.dto.request.AdminLoginRequest;
import lk.ac.ruhuna.dcs.cvmanagement.modules.auth.api.dto.request.StudentLoginRequest;
import lk.ac.ruhuna.dcs.cvmanagement.modules.auth.api.dto.response.AuthTokenResponse;
import lk.ac.ruhuna.dcs.cvmanagement.modules.auth.api.dto.response.CurrentUserResponse;
import lk.ac.ruhuna.dcs.cvmanagement.modules.auth.application.AuthService;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
@Validated
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/student/login")
    public AuthTokenResponse loginStudent(@Valid @RequestBody StudentLoginRequest request) {
        return authService.loginStudent(request);
    }

    @PostMapping("/admin/login")
    public AuthTokenResponse loginAdmin(@Valid @RequestBody AdminLoginRequest request) {
        return authService.loginAdmin(request);
    }

    @GetMapping("/me")
    public CurrentUserResponse me() {
        return authService.me();
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout() {
        authService.logout();
        return ResponseEntity.noContent().build();
    }
}
