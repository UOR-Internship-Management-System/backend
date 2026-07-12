package lk.ac.ruhuna.dcs.cvmanagement.modules.auth.application;

import java.util.Locale;
import java.util.UUID;
import lk.ac.ruhuna.dcs.cvmanagement.modules.verification.api.dto.request.PasswordCreateRequest;
import lk.ac.ruhuna.dcs.cvmanagement.modules.verification.api.dto.request.PasswordResetStartRequest;
import lk.ac.ruhuna.dcs.cvmanagement.modules.verification.api.dto.response.OtpResendResponse;
import lk.ac.ruhuna.dcs.cvmanagement.modules.verification.api.dto.response.OtpVerifyResponse;
import lk.ac.ruhuna.dcs.cvmanagement.modules.verification.api.dto.response.PasswordResetResponse;
import lk.ac.ruhuna.dcs.cvmanagement.modules.verification.application.OtpService;
import lk.ac.ruhuna.dcs.cvmanagement.modules.verification.domain.policy.OtpPurpose;
import lk.ac.ruhuna.dcs.cvmanagement.shared.audit.AuditEventPublisher;
import lk.ac.ruhuna.dcs.cvmanagement.shared.error.BadRequestException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PasswordResetService {

    private static final String SAFE_RESET_MESSAGE =
            "If the account is eligible, a password reset OTP has been sent.";

    private final AuthService authService;
    private final OtpService otpService;
    private final AuditEventPublisher auditEventPublisher;

    public PasswordResetService(
            AuthService authService,
            OtpService otpService,
            AuditEventPublisher auditEventPublisher) {
        this.authService = authService;
        this.otpService = otpService;
        this.auditEventPublisher = auditEventPublisher;
    }

    @Transactional
    public PasswordResetResponse start(PasswordResetStartRequest request) {
        String email = request.email().trim().toLowerCase(Locale.ROOT);
        return authService.findResetEligible(request.accountType(), email)
                .map(account -> {
                    OtpService.OtpCreateResult result =
                            otpService.createResetContext(account.id(), request.accountType(), email);
                    auditEventPublisher.record(
                            account.id(),
                            request.accountType().name(),
                            "AUTH_PASSWORD_RESET_STARTED",
                            "user_account",
                            account.id().toString());
                    return new PasswordResetResponse(result.contextId(), SAFE_RESET_MESSAGE, result.ttl().toSeconds());
                })
                .orElseGet(() -> {
                    auditEventPublisher.record(
                            null,
                            request.accountType().name(),
                            "AUTH_PASSWORD_RESET_REQUEST_NON_ELIGIBLE",
                            "user_account",
                            null);
                    return new PasswordResetResponse(null, SAFE_RESET_MESSAGE, null);
                });
    }

    public OtpVerifyResponse verifyOtp(UUID resetId, String otp) {
        return otpService.verify(resetId, OtpPurpose.PASSWORD_RESET, otp);
    }

    public OtpResendResponse resendOtp(UUID resetId) {
        return otpService.resend(resetId, OtpPurpose.PASSWORD_RESET);
    }

    @Transactional
    public void complete(UUID resetId, PasswordCreateRequest request) {
        if (!request.newPassword().equals(request.confirmPassword())) {
            throw new BadRequestException("Password confirmation does not match.");
        }
        OtpService.OtpContext context = otpService.requireVerified(resetId, OtpPurpose.PASSWORD_RESET);
        if (context.userAccountId() == null || context.accountType() == null) {
            throw new BadRequestException("Password reset context is invalid.");
        }
        AuthService.AccountRecord account = authService.findResetEligible(context.accountType(), context.email())
                .filter(candidate -> candidate.id().equals(context.userAccountId()))
                .orElseThrow(() -> new BadRequestException("Password reset context is invalid."));
        authService.updatePassword(account.id(), request.newPassword());
        otpService.consume(context.id());
        auditEventPublisher.record(
                account.id(),
                context.accountType().name(),
                "AUTH_PASSWORD_RESET_COMPLETED",
                "user_account",
                account.id().toString());
    }
}
