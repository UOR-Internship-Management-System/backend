package lk.ac.ruhuna.dcs.cvmanagement.infrastructure.email;

import java.time.Instant;
import java.util.Arrays;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

@Component
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
