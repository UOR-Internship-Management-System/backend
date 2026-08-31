package lk.ac.ruhuna.dcs.cvmanagement.infrastructure.email;

import java.time.Duration;
import java.time.Instant;
import org.springframework.stereotype.Component;

/**
 * Renders plain-text bodies for transactional OTP messages.
 *
 * <p>Plain text is used deliberately: it avoids HTML-injection risk from any interpolated value and
 * renders reliably in university webmail clients.
 */
@Component
public class EmailTemplateRenderer {

    private static final String SIGN_UP = "SIGN_UP";
    private static final String PASSWORD_RESET = "PASSWORD_RESET";

    public String subjectFor(String purpose) {
        return switch (purpose) {
            case SIGN_UP -> "Verify your CV Management account";
            case PASSWORD_RESET -> "Reset your CV Management password";
            default -> "Your CV Management verification code";
        };
    }

    public String otpBody(String purpose, String otp, Instant expiresAt, Instant now) {
        long minutes = Math.max(1, Duration.between(now, expiresAt).toMinutes());
        String action = switch (purpose) {
            case SIGN_UP -> "complete your account registration";
            case PASSWORD_RESET -> "reset your password";
            default -> "verify your identity";
        };

        return """
                Department of Computer Science
                University of Ruhuna — CV Management

                Use the following verification code to %s:

                    %s

                This code expires in %d minute(s). Do not share it with anyone.

                If you did not request this, you can safely ignore this email — no
                changes have been made to your account.

                This is an automated message. Please do not reply.
                """
            .formatted(action, otp, minutes);
    }
}
