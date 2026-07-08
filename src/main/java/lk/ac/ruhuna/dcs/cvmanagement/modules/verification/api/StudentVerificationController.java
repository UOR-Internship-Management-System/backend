package lk.ac.ruhuna.dcs.cvmanagement.modules.verification.api;

import jakarta.validation.Valid;
import java.net.URI;
import java.util.UUID;
import lk.ac.ruhuna.dcs.cvmanagement.modules.verification.api.dto.request.OtpVerifyRequest;
import lk.ac.ruhuna.dcs.cvmanagement.modules.verification.api.dto.request.PasswordCreateRequest;
import lk.ac.ruhuna.dcs.cvmanagement.modules.verification.api.dto.request.StudentVerificationStartRequest;
import lk.ac.ruhuna.dcs.cvmanagement.modules.verification.api.dto.response.OtpResendResponse;
import lk.ac.ruhuna.dcs.cvmanagement.modules.verification.api.dto.response.OtpVerifyResponse;
import lk.ac.ruhuna.dcs.cvmanagement.modules.verification.api.dto.response.StudentVerificationResponse;
import lk.ac.ruhuna.dcs.cvmanagement.modules.verification.application.StudentVerificationService;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/student-verifications")
@Validated
public class StudentVerificationController {

    private final StudentVerificationService studentVerificationService;

    public StudentVerificationController(StudentVerificationService studentVerificationService) {
        this.studentVerificationService = studentVerificationService;
    }

    @PostMapping
    public ResponseEntity<StudentVerificationResponse> start(
            @Valid @RequestBody StudentVerificationStartRequest request) {
        StudentVerificationResponse response = studentVerificationService.start(request);
        return ResponseEntity
                .created(URI.create("/api/v1/student-verifications/" + response.verificationId()))
                .body(response);
    }

    @PostMapping("/{verificationId}/otp/verify")
    public OtpVerifyResponse verifyOtp(
            @PathVariable UUID verificationId,
            @Valid @RequestBody OtpVerifyRequest request) {
        return studentVerificationService.verifyOtp(verificationId, request.otp());
    }

    @PostMapping("/{verificationId}/otp/resend")
    public OtpResendResponse resendOtp(@PathVariable UUID verificationId) {
        return studentVerificationService.resendOtp(verificationId);
    }

    @PostMapping("/{verificationId}/password")
    public ResponseEntity<Void> createPassword(
            @PathVariable UUID verificationId,
            @Valid @RequestBody PasswordCreateRequest request) {
        studentVerificationService.createPassword(verificationId, request);
        return ResponseEntity.noContent().build();
    }
}
