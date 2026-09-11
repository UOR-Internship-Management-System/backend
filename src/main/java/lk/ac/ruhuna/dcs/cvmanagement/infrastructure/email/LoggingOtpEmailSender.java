package lk.ac.ruhuna.dcs.cvmanagement.infrastructure.email;

import jakarta.annotation.PostConstruct;
import java.time.Instant;
import java.util.Arrays;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

/**
 * Development OTP sink. Writes the code to the application log instead of sending mail.
 *
 * <p>Active only when {@code app.email.mode} is absent or set to {@code log}. Startup fails if this
 * adapter is selected under the {@code prod} profile — a silently discarded OTP is worse than a
 * refusal to boot.
 */
@Component
@ConditionalOnProperty(name = "app.email.mode", havingValue = "log", matchIfMissing = true)
public class LoggingOtpEmailSender implements OtpEmailSender {

    private static final Logger LOGGER = LoggerFactory.getLogger(LoggingOtpEmailSender.class);

    private final Environment environment;
    private final boolean logOtp;

    public LoggingOtpEmailSender(
        Environment environment,
        @Value("${app.email.log-otp:false}") boolean logOtp) {
        this.environment = environment;
        this.logOtp = logOtp;
    }

    @PostConstruct
    void rejectProductionUse() {
        boolean production = Arrays.stream(environment.getActiveProfiles())
            .anyMatch(profile -> profile.equals("prod"));
        if (production) {
            throw new IllegalStateException(
                "LoggingOtpEmailSender is active under the 'prod' profile. OTP emails would be "
                    + "discarded and no student could register or reset a password. Set "
                    + "app.email.mode=smtp and configure spring.mail.* before deploying.");
        }
    }

    @Override
    public void sendOtp(String recipientEmail, String purpose, String otp, Instant expiresAt) {
        if (logOtp && isDevLikeProfile()) {
            LOGGER.info(
                "DEV-ONLY OTP delivery for purpose={} recipient={} expiresAt={} otp={}",
                purpose,
                maskEmail(recipientEmail),
                expiresAt,
                otp);
            return;
        }
        LOGGER.info(
            "OTP delivery requested for purpose={} recipient={} expiresAt={}",
            purpose,
            maskEmail(recipientEmail),
            expiresAt);
    }

    private boolean isDevLikeProfile() {
        return Arrays.stream(environment.getActiveProfiles())
            .anyMatch(profile -> profile.equals("local") || profile.equals("dev") || profile.equals("test"));
    }

    private String maskEmail(String email) {
        int at = email == null ? -1 : email.indexOf('@');
        if (at <= 1) {
            return "***";
        }
        return email.charAt(0) + "***" + email.substring(at);
    }
}
