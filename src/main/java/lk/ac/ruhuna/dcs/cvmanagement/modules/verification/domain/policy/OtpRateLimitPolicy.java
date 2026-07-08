package lk.ac.ruhuna.dcs.cvmanagement.modules.verification.domain.policy;

import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.auth.otp")
public record OtpRateLimitPolicy(
        int length,
        Duration ttl,
        int maxAttempts,
        Duration resendCooldown,
        int maxResends) {

    public OtpRateLimitPolicy {
        if (length <= 0) {
            length = 6;
        }
        if (ttl == null || ttl.isNegative() || ttl.isZero()) {
            ttl = Duration.ofMinutes(5);
        }
        if (maxAttempts <= 0) {
            maxAttempts = 3;
        }
        if (resendCooldown == null || resendCooldown.isNegative()) {
            resendCooldown = Duration.ofSeconds(60);
        }
        if (maxResends <= 0) {
            maxResends = 3;
        }
    }
}
