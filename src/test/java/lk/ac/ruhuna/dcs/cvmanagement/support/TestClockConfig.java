package lk.ac.ruhuna.dcs.cvmanagement.support;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

/**
 * Test configuration providing a fixed {@link Clock} for deterministic
 * time-dependent tests.
 */
@TestConfiguration
public class TestClockConfig {

    public static final Instant FIXED_INSTANT = Instant.parse("2026-01-15T10:00:00Z");

    @Bean
    Clock testClock() {
        return Clock.fixed(FIXED_INSTANT, ZoneOffset.UTC);
    }
}
