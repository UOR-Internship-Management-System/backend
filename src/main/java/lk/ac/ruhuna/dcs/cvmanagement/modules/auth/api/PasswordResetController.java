package lk.ac.ruhuna.dcs.cvmanagement.modules.auth.api;

import jakarta.validation.Valid;
import java.net.URI;
import java.util.UUID;
import lk.ac.ruhuna.dcs.cvmanagement.modules.auth.application.PasswordResetService;
import lk.ac.ruhuna.dcs.cvmanagement.modules.verification.api.dto.request.OtpVerifyRequest;
import lk.ac.ruhuna.dcs.cvmanagement.modules.verification.api.dto.request.PasswordCreateRequest;
import lk.ac.ruhuna.dcs.cvmanagement.modules.verification.api.dto.request.PasswordResetStartRequest;
import lk.ac.ruhuna.dcs.cvmanagement.modules.verification.api.dto.response.OtpResendResponse;
import lk.ac.ruhuna.dcs.cvmanagement.modules.verification.api.dto.response.OtpVerifyResponse;
import lk.ac.ruhuna.dcs.cvmanagement.modules.verification.api.dto.response.PasswordResetResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/password-resets")
@Validated
public class PasswordResetController {

    private final PasswordResetService passwordResetService;

    public PasswordResetController(PasswordResetService passwordResetService) {
        this.passwordResetService = passwordResetService;
    }

    @PostMapping
    public ResponseEntity<PasswordResetResponse> start(@Valid @RequestBody PasswordResetStartRequest request) {
        PasswordResetResponse response = passwordResetService.start(request);
        if (response.resetId() == null) {
            return ResponseEntity.accepted().body(response);
        }
        return ResponseEntity
                .created(URI.create("/api/v1/password-resets/" + response.resetId()))
                .body(response);
    }

    @PostMapping("/{resetId}/otp/verify")
    public OtpVerifyResponse verifyOtp(
            @PathVariable UUID resetId,
            @Valid @RequestBody OtpVerifyRequest request) {
        return passwordResetService.verifyOtp(resetId, request.otp());
    }

    @PostMapping("/{resetId}/otp/resend")
    public OtpResendResponse resendOtp(@PathVariable UUID resetId) {
        return passwordResetService.resendOtp(resetId);
    }

    @PostMapping("/{resetId}/password")
    public ResponseEntity<Void> complete(
            @PathVariable UUID resetId,
            @Valid @RequestBody PasswordCreateRequest request) {
        passwordResetService.complete(resetId, request);
        return ResponseEntity.noContent().build();
    }
}
