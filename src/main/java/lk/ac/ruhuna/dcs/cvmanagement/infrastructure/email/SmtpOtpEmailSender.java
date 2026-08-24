package lk.ac.ruhuna.dcs.cvmanagement.infrastructure.email;

import java.time.Clock;
import java.time.Instant;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * Delivers OTP codes over SMTP.
 *
 * <p>Active only when {@code app.email.mode=smtp}; otherwise {@link LoggingOtpEmailSender} is the
 * sole {@link OtpEmailSender} bean.
 */
@Component
@ConditionalOnProperty(name = "app.email.mode", havingValue = "smtp")
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
        emailSender.send(
            recipientEmail,
            templateRenderer.subjectFor(purpose),
            templateRenderer.otpBody(purpose, otp, expiresAt, Instant.now(clock)));
    }
}
