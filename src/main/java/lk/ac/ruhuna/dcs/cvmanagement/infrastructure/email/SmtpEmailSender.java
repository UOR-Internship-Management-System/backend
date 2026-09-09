package lk.ac.ruhuna.dcs.cvmanagement.infrastructure.email;

import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

/**
 * SMTP-based email sending adapter.
 *
 * <p>Active only when {@code app.email.mode=smtp}. Failures propagate as
 * {@link EmailDeliveryException} so the caller never reports success on a message that was not
 * accepted by the transport.
 *
 * <p>Sends multipart/alternative: a plain-text part and an HTML part. Passing the plain-text
 * argument first to {@link MimeMessageHelper#setText(String, String)} is what produces
 * multipart/alternative rather than HTML-only, which improves deliverability and gives clients
 * that block HTML a readable fallback.
 */
@Component
@ConditionalOnProperty(name = "app.email.mode", havingValue = "smtp")
public class SmtpEmailSender implements EmailSender {

    private static final Logger LOGGER = LoggerFactory.getLogger(SmtpEmailSender.class);

    private final JavaMailSender mailSender;
    private final String fromAddress;

    public SmtpEmailSender(
        JavaMailSender mailSender,
        @Value("${app.email.from}") String fromAddress) {
        this.mailSender = mailSender;
        this.fromAddress = fromAddress;
    }

    @Override
    public void send(String recipientEmail, String subject, String textBody, String htmlBody) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromAddress);
            helper.setTo(recipientEmail);
            helper.setSubject(subject);
            // Plain text first, HTML second: this produces multipart/alternative.
            helper.setText(textBody, htmlBody);

            mailSender.send(message);
            LOGGER.info("Transactional email dispatched to recipient={}", maskEmail(recipientEmail));
        } catch (Exception exception) {
            // Never log the body — it contains the OTP.
            LOGGER.error(
                "Transactional email delivery failed for recipient={}",
                maskEmail(recipientEmail),
                exception);
            throw new EmailDeliveryException("Unable to deliver transactional email.", exception);
        }
    }

    private String maskEmail(String email) {
        int at = email == null ? -1 : email.indexOf('@');
        if (at <= 1) {
            return "***";
        }
        return email.charAt(0) + "***" + email.substring(at);
    }
}
