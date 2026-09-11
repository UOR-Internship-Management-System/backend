package lk.ac.ruhuna.dcs.cvmanagement.infrastructure.email;

import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

/**
 * Sends transactional email through Brevo's HTTPS API (api.brevo.com) rather than SMTP.
 *
 * <p>Some hosts (e.g. Railway trial-tier projects) block outbound SMTP ports entirely, so this
 * adapter reaches Brevo over plain HTTPS instead. Active only when {@code app.email.mode=brevo-api}.
 */
@Component
@ConditionalOnProperty(name = "app.email.mode", havingValue = "brevo-api")
public class BrevoApiEmailSender implements EmailSender {

    private static final Logger LOGGER = LoggerFactory.getLogger(BrevoApiEmailSender.class);
    private static final String BREVO_ENDPOINT = "https://api.brevo.com/v3/smtp/email";

    private final RestClient restClient;
    private final String fromAddress;

    public BrevoApiEmailSender(
        RestClient.Builder restClientBuilder,
        @Value("${app.email.from}") String fromAddress,
        @Value("${app.email.brevo-api-key}") String apiKey) {
        this.fromAddress = fromAddress;
        this.restClient = restClientBuilder
            .baseUrl(BREVO_ENDPOINT)
            .defaultHeader("api-key", apiKey)
            .defaultHeader("accept", "application/json")
            .build();
    }

    @Override
    public void send(String recipientEmail, String subject, String textBody, String htmlBody) {
        try {
            restClient.post()
                .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                .body(Map.of(
                    "sender", Map.of("email", fromAddress),
                    "to", List.of(Map.of("email", recipientEmail)),
                    "subject", subject,
                    "textContent", textBody,
                    "htmlContent", htmlBody))
                .retrieve()
                .toBodilessEntity();
            LOGGER.info("Transactional email dispatched to recipient={}", maskEmail(recipientEmail));
        } catch (RestClientException exception) {
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
