package lk.ac.ruhuna.dcs.cvmanagement.infrastructure.email;

import java.time.Clock;
import java.time.Instant;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.stereotype.Component;

/**
 * Delivers OTP codes through whichever {@link EmailSender} transport is active.
 *
 * <p>Active when {@code app.email.mode} is {@code smtp} or {@code brevo-api}; otherwise
 * {@link LoggingOtpEmailSender} is the sole {@link OtpEmailSender} bean.
 */
@Component
@ConditionalOnExpression(
    "'${app.email.mode:log}' == 'smtp' or '${app.email.mode:log}' == 'brevo-api'")
public class SmtpOtpEmailSender implements OtpEmailSender {

    private final EmailSender emailSender;
    private final EmailTemplateRenderer templateRenderer;
    private final Clock clock;

    public SmtpOtpEmailSender(
        EmailSender emailSender,
        EmailTemplateRenderer templateRenderer,
        Clock clock) {
        this.emailSender = emailSender;
        this.templateRenderer = templateRenderer;
        this.clock = clock;
    }

    @Override
    public void sendOtp(String recipientEmail, String purpose, String otp, Instant expiresAt) {
        Instant now = Instant.now(clock);
        emailSender.send(
            recipientEmail,
            templateRenderer.subjectFor(purpose),
            templateRenderer.otpBody(purpose, otp, expiresAt, now),
            templateRenderer.otpBodyHtml(purpose, otp, expiresAt, now));
    }
}
