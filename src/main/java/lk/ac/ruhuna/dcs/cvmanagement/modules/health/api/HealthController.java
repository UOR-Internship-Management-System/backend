package lk.ac.ruhuna.dcs.cvmanagement.modules.health.api;

import java.time.Instant;
import lk.ac.ruhuna.dcs.cvmanagement.shared.api.ApiPaths;
import org.flywaydb.core.Flyway;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HealthController {

    private final JdbcTemplate jdbcTemplate;
    private final ObjectProvider<Flyway> flyway;

    public HealthController(JdbcTemplate jdbcTemplate, ObjectProvider<Flyway> flyway) {
        this.jdbcTemplate = jdbcTemplate;
        this.flyway = flyway;
    }

    @GetMapping(ApiPaths.HEALTH)
    HealthResponse health() {
        Integer databaseResult = jdbcTemplate.queryForObject("SELECT 1", Integer.class);
        int appliedMigrations = flyway.stream()
                .findFirst()
                .map(activeFlyway -> activeFlyway.info().applied().length)
                .orElse(0);
        return new HealthResponse(
                "UP",
                "cv-management-backend",
                Instant.now(),
                databaseResult != null && databaseResult == 1 ? "UP" : "DOWN",
                appliedMigrations);
    }

    record HealthResponse(
            String status,
            String service,
            Instant timestamp,
            String database,
            int appliedMigrations) {
    }
}
