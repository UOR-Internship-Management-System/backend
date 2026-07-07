package lk.ac.ruhuna.dcs.cvmanagement.infrastructure.jwt;

import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.security.jwt")
public record JwtProperties(
        String issuer,
        String secret,
        Duration accessTokenTtl,
        Duration refreshTokenTtl) {
}
