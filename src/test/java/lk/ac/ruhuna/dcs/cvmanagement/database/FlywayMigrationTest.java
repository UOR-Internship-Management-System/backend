package lk.ac.ruhuna.dcs.cvmanagement.database;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import javax.sql.DataSource;
import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@Testcontainers(disabledWithoutDocker = true)
class FlywayMigrationTest {

    private static final int LATEST_MIGRATION_COUNT = 28;

    @Container
    private static final PostgreSQLContainer<?> POSTGRES =
            new PostgreSQLContainer<>("postgres:16-alpine")
                    .withDatabaseName("cv_management_test")
                    .withUsername("cv_user")
                    .withPassword("cv_local_password");

    private DataSource dataSource;

    @BeforeEach
    void resetDatabase() throws SQLException {
        dataSource = DataSourceBuilder.create()
                .url(POSTGRES.getJdbcUrl())
                .username(POSTGRES.getUsername())
                .password(POSTGRES.getPassword())
                .driverClassName(POSTGRES.getDriverClassName())
                .build();

        try (Connection connection = dataSource.getConnection(); Statement statement = connection.createStatement()) {
            statement.execute("DROP SCHEMA IF EXISTS academic CASCADE");
            statement.execute("DROP SCHEMA IF EXISTS ref CASCADE");
            statement.execute("DROP SCHEMA IF EXISTS system CASCADE");
            statement.execute("DROP SCHEMA public CASCADE");
            statement.execute("CREATE SCHEMA public");
        }
    }

    @Test
    void migrationsRunFromEmptyPostgresDatabase() {
        Flyway flyway = flyway();

        assertThat(flyway.migrate().success).isTrue();
        assertThat(flyway.info().applied()).hasSize(LATEST_MIGRATION_COUNT);

        JdbcTemplate jdbc = new JdbcTemplate(dataSource);
        assertThat(tableExists("academic", "academic_ledger_upload")).isTrue();
        assertThat(tableExists("academic", "academic_ledger_staging_row")).isTrue();
        assertThat(tableExists("academic", "academic_ledger_validation_error")).isTrue();
        assertThat(tableExists("academic", "official_student_grade")).isTrue();
        assertThat(tableExists("academic", "student_academic_summary")).isTrue();
        assertThat(tableExists("academic", "subject")).isTrue();
        assertThat(tableExists("ref", "grade_scale")).isTrue();
        assertThat(tableExists("system", "file_asset")).isTrue();
        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM ref.grade_scale", Integer.class)).isEqualTo(12);
        assertThat(jdbc.queryForObject(
                        "SELECT grade_point FROM ref.grade_scale WHERE grade_code = 'A-'", java.math.BigDecimal.class))
                .isEqualByComparingTo("3.70");
    }


    @Test
    void duplicateActiveFileHashIsRejectedButFailedUploadCanBeRetried() {
        assertThat(flyway().migrate().success).isTrue();
        JdbcTemplate jdbc = new JdbcTemplate(dataSource);

        jdbc.update("""
                INSERT INTO public.user_accounts (id, university_email, account_status)
                VALUES ('10000000-0000-0000-0000-000000000001', 'admin@dcs.ruh.ac.lk', 'ACTIVE')
                """);

        jdbc.update("""
                INSERT INTO system.file_asset (
                    file_asset_id, owner_account_id, file_name, storage_key, mime_type, file_size_bytes, checksum_sha256
                ) VALUES (
                    '20000000-0000-0000-0000-000000000001',
                    '10000000-0000-0000-0000-000000000001',
                    'first.csv', 'academic-ledger/first.csv', 'text/csv', 32,
                    'aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa'
                )
                """);
        jdbc.update("""
                INSERT INTO system.file_asset (
                    file_asset_id, owner_account_id, file_name, storage_key, mime_type, file_size_bytes, checksum_sha256
                ) VALUES (
                    '20000000-0000-0000-0000-000000000002',
                    '10000000-0000-0000-0000-000000000001',
                    'second.csv', 'academic-ledger/second.csv', 'text/csv', 32,
                    'bbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbb'
                )
                """);

        String duplicateHash = "cccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccc";
        jdbc.update("""
                INSERT INTO academic.academic_ledger_upload (
                    academic_ledger_upload_id, uploaded_by_account_id, source_file_asset_id,
                    file_name, file_hash, upload_status, validation_status
                ) VALUES (
                    '30000000-0000-0000-0000-000000000001',
                    '10000000-0000-0000-0000-000000000001',
                    '20000000-0000-0000-0000-000000000001',
                    'first.csv', ?, 'RECEIVED', 'NOT_STARTED'
                )
                """, duplicateHash);

        assertThatThrownBy(() -> jdbc.update("""
                INSERT INTO academic.academic_ledger_upload (
                    academic_ledger_upload_id, uploaded_by_account_id, source_file_asset_id,
                    file_name, file_hash, upload_status, validation_status
                ) VALUES (
                    '30000000-0000-0000-0000-000000000002',
                    '10000000-0000-0000-0000-000000000001',
                    '20000000-0000-0000-0000-000000000002',
                    'second.csv', ?, 'RECEIVED', 'NOT_STARTED'
                )
                """, duplicateHash))
                .isInstanceOf(DataIntegrityViolationException.class);

        jdbc.update("""
                UPDATE academic.academic_ledger_upload
                SET upload_status = 'PROCESSING_FAILED'
                WHERE academic_ledger_upload_id = '30000000-0000-0000-0000-000000000001'
                """);

        assertThat(jdbc.update("""
                INSERT INTO academic.academic_ledger_upload (
                    academic_ledger_upload_id, uploaded_by_account_id, source_file_asset_id,
                    file_name, file_hash, upload_status, validation_status
                ) VALUES (
                    '30000000-0000-0000-0000-000000000002',
                    '10000000-0000-0000-0000-000000000001',
                    '20000000-0000-0000-0000-000000000002',
                    'second.csv', ?, 'RECEIVED', 'NOT_STARTED'
                )
                """, duplicateHash)).isEqualTo(1);
    }

    @Test
    void academicFoundationUpgradesExistingVersion22Database() {
        Flyway throughVersion22 = Flyway.configure()
                .dataSource(dataSource)
                .locations("classpath:db/migration")
                .target("22")
                .load();

        assertThat(throughVersion22.migrate().success).isTrue();
        assertThat(throughVersion22.info().applied()).hasSize(22);

        Flyway latest = flyway();
        assertThat(latest.migrate().success).isTrue();
        assertThat(latest.info().applied()).hasSize(LATEST_MIGRATION_COUNT);
        assertThat(tableExists("academic", "official_student_grade")).isTrue();
    }

    private Flyway flyway() {
        return Flyway.configure()
                .dataSource(dataSource)
                .locations("classpath:db/migration")
                .load();
    }

    private boolean tableExists(String schema, String table) {
        try (Connection connection = dataSource.getConnection();
                ResultSet resultSet = connection.getMetaData().getTables(null, schema, table, new String[] {"TABLE"})) {
            return resultSet.next();
        } catch (SQLException exception) {
            throw new IllegalStateException("Failed to inspect migrated PostgreSQL schema", exception);
        }
    }
}
