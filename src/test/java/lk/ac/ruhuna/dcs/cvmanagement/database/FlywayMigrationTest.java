package lk.ac.ruhuna.dcs.cvmanagement.database;

import static org.assertj.core.api.Assertions.assertThat;

import javax.sql.DataSource;
import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.Test;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@Testcontainers(disabledWithoutDocker = true)
class FlywayMigrationTest {

    @Container
    private static final PostgreSQLContainer<?> POSTGRES =
            new PostgreSQLContainer<>("postgres:16-alpine")
                    .withDatabaseName("cv_management_test")
                    .withUsername("cv_user")
                    .withPassword("cv_local_password");

    @Test
    void migrationsRunFromEmptyPostgresDatabase() {
        DataSource dataSource = DataSourceBuilder.create()
                .url(POSTGRES.getJdbcUrl())
                .username(POSTGRES.getUsername())
                .password(POSTGRES.getPassword())
                .driverClassName(POSTGRES.getDriverClassName())
                .build();

        Flyway flyway = Flyway.configure()
                .dataSource(dataSource)
                .locations("classpath:db/migration")
                .load();

        assertThat(flyway.migrate().success).isTrue();
        assertThat(flyway.info().applied()).hasSize(22);
    }
}
