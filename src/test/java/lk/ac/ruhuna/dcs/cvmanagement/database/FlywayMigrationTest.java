package lk.ac.ruhuna.dcs.cvmanagement.database;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.UUID;
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

    private static final int LATEST_MIGRATION_COUNT = 37;

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
        assertThat(tableExists("public", "companies")).isTrue();
        assertThat(tableExists("public", "internship_requests")).isTrue();
        assertThat(tableExists("public", "internship_request_skills")).isTrue();
        assertThat(columnExists("public", "companies", "active")).isFalse();
        assertThat(columnExists("public", "internship_requests", "status")).isFalse();
        assertThat(columnExists("public", "internship_requests", "minimum_gpa")).isFalse();
        assertThat(columnExists("public", "internship_requests", "maximum_gpa")).isFalse();
        assertThat(columnExists("public", "internship_requests", "required_gpa")).isFalse();
        assertThat(columnExists("public", "internship_requests", "gpa_range")).isFalse();
        assertThat(columnExists("public", "internship_requests", "location")).isFalse();
        assertThat(columnExists("public", "internship_requests", "work_mode")).isFalse();
        assertThat(columnExists("public", "internship_requests", "request_notes")).isFalse();
        assertThat(columnExists("public", "internship_requests", "required_competency_level")).isFalse();
        assertThat(columnExists("public", "internship_request_skills", "required_competency_level")).isFalse();
        assertThat(jdbc.queryForObject(
                        "SELECT EXISTS (SELECT 1 FROM pg_indexes "
                                + "WHERE schemaname = 'public' AND indexname = 'idx_companies_name_id')",
                        Boolean.class))
                .isTrue();
        assertThat(jdbc.queryForObject(
                        "SELECT EXISTS (SELECT 1 FROM pg_indexes "
                                + "WHERE schemaname = 'public' "
                                + "AND indexname = 'idx_internship_requests_company_created_at_id')",
                        Boolean.class))
                .isTrue();
        assertThat(jdbc.queryForObject(
                        "SELECT EXISTS (SELECT 1 FROM pg_indexes "
                                + "WHERE schemaname = 'public' "
                                + "AND indexname = 'idx_internship_request_skills_skill_id')",
                        Boolean.class))
                .isTrue();
        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM ref.grade_scale", Integer.class)).isEqualTo(12);
        assertThat(jdbc.queryForObject(
                        "SELECT grade_point FROM ref.grade_scale WHERE grade_code = 'A-'", java.math.BigDecimal.class))
                .isEqualByComparingTo("3.70");
    }



    @Test
    void version55RejectsLegacyRowsThatViolateTheCanonicalSupportingDataContract() {
        Flyway throughVersion54 = Flyway.configure()
                .dataSource(dataSource)
                .locations("classpath:db/migration")
                .target("54")
                .load();
        assertThat(throughVersion54.migrate().success).isTrue();

        JdbcTemplate jdbc = new JdbcTemplate(dataSource);
        UUID studentId = UUID.fromString("91000000-0000-4000-8000-000000000001");
        jdbc.update("""
                INSERT INTO public.eligible_students (
                    id, index_number, university_email, full_name, academic_level, is_active
                ) VALUES (?, 'SC/2026/00001', 'sc202600001@dcs.ruh.ac.lk', 'V055 Legacy Student', 3, TRUE)
                """, studentId);
        jdbc.update("""
                INSERT INTO public.student_certificates (id, student_id, title, issuer, issue_date)
                VALUES ('92000000-0000-4000-8000-000000000001', ?, 'Legacy Certificate', NULL, NULL)
                """, studentId);

        assertThatThrownBy(() -> flyway().migrate())
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("V055 preflight failed");
    }

    @Test
    void duplicateActiveFileHashIsRejectedButFailedUploadCanBeRetried() {
        assertThat(flyway().migrate().success).isTrue();
        JdbcTemplate jdbc = new JdbcTemplate(dataSource);

        jdbc.update("""
                INSERT INTO public.user_accounts (id, university_email, account_status)
                VALUES ('10000000-0000-0000-0000-000000000001', 'flyway.ledger.admin@dcs.ruh.ac.lk', 'ACTIVE')
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
    void companyInternshipFoundationUpgradesExistingVersion55Database() {
        Flyway throughVersion55 = Flyway.configure()
                .dataSource(dataSource)
                .locations("classpath:db/migration")
                .target("55")
                .load();

        assertThat(throughVersion55.migrate().success).isTrue();
        assertThat(throughVersion55.info().applied()).hasSize(33);
        assertThat(tableExists("public", "companies")).isFalse();
        assertThat(tableExists("public", "internship_requests")).isFalse();

        Flyway latest = flyway();
        assertThat(latest.migrate().success).isTrue();
        assertThat(latest.info().applied()).hasSize(LATEST_MIGRATION_COUNT);
        assertThat(tableExists("public", "companies")).isTrue();
        assertThat(tableExists("public", "internship_requests")).isTrue();
        assertThat(tableExists("public", "internship_request_skills")).isTrue();
    }

    @Test
    void normalizedCompanyNameIsDatabaseDerivedAndUniqueAcrossCaseAndWhitespace() {
        assertThat(flyway().migrate().success).isTrue();
        JdbcTemplate jdbc = new JdbcTemplate(dataSource);

        UUID companyId = UUID.fromString("41000000-0000-4000-8000-000000000001");
        jdbc.update("INSERT INTO companies (id, name) VALUES (?, ?)", companyId, "  Example   Technologies  ");

        assertThat(jdbc.queryForObject(
                        "SELECT normalized_name FROM companies WHERE id = ?", String.class, companyId))
                .isEqualTo("example technologies");

        assertThatThrownBy(() -> jdbc.update(
                        "INSERT INTO companies (id, name) VALUES (?, ?)",
                        UUID.fromString("41000000-0000-4000-8000-000000000002"),
                        "example technologies"))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void companyDeleteCascadesRequestsAndAssociationsButPreservesCanonicalSkills() {
        assertThat(flyway().migrate().success).isTrue();
        JdbcTemplate jdbc = new JdbcTemplate(dataSource);

        UUID companyId = UUID.fromString("42000000-0000-4000-8000-000000000001");
        UUID requestId = UUID.fromString("43000000-0000-4000-8000-000000000001");
        UUID requiredSkillId = UUID.fromString("44000000-0000-4000-8000-000000000001");
        UUID skillId = jdbc.queryForObject(
                "SELECT id FROM skills WHERE skill_status = 'ACTIVE' ORDER BY skill_name LIMIT 1", UUID.class);

        jdbc.update("INSERT INTO companies (id, name) VALUES (?, 'Cascade Test Company')", companyId);
        jdbc.update(
                "INSERT INTO internship_requests (id, company_id, title) VALUES (?, ?, 'Backend Intern')",
                requestId,
                companyId);
        jdbc.update(
                "INSERT INTO internship_request_skills (id, internship_request_id, skill_id) VALUES (?, ?, ?)",
                requiredSkillId,
                requestId,
                skillId);

        assertThat(jdbc.update("DELETE FROM companies WHERE id = ?", companyId)).isEqualTo(1);
        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM companies WHERE id = ?", Integer.class, companyId))
                .isZero();
        assertThat(jdbc.queryForObject(
                        "SELECT COUNT(*) FROM internship_requests WHERE id = ?", Integer.class, requestId))
                .isZero();
        assertThat(jdbc.queryForObject(
                        "SELECT COUNT(*) FROM internship_request_skills WHERE id = ?", Integer.class, requiredSkillId))
                .isZero();
        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM skills WHERE id = ?", Integer.class, skillId))
                .isEqualTo(1);
    }

    @Test
    void canonicalSkillDeletionIsRestrictedWhileReferencedByInternshipRequest() {
        assertThat(flyway().migrate().success).isTrue();
        JdbcTemplate jdbc = new JdbcTemplate(dataSource);

        UUID companyId = UUID.fromString("45000000-0000-4000-8000-000000000001");
        UUID requestId = UUID.fromString("46000000-0000-4000-8000-000000000001");
        UUID skillId = jdbc.queryForObject(
                "SELECT id FROM skills WHERE skill_status = 'ACTIVE' ORDER BY skill_name LIMIT 1", UUID.class);

        jdbc.update("INSERT INTO companies (id, name) VALUES (?, 'Skill Restrict Company')", companyId);
        jdbc.update(
                "INSERT INTO internship_requests (id, company_id, title) VALUES (?, ?, 'Platform Intern')",
                requestId,
                companyId);
        jdbc.update(
                "INSERT INTO internship_request_skills (internship_request_id, skill_id) VALUES (?, ?)",
                requestId,
                skillId);

        assertThatThrownBy(() -> jdbc.update("DELETE FROM skills WHERE id = ?", skillId))
                .isInstanceOf(DataIntegrityViolationException.class);
        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM skills WHERE id = ?", Integer.class, skillId))
                .isEqualTo(1);
    }

    @Test
    void companyInternshipConstraintsRejectInvalidRowsAndDuplicateRequiredSkills() {
        assertThat(flyway().migrate().success).isTrue();
        JdbcTemplate jdbc = new JdbcTemplate(dataSource);

        assertThatThrownBy(() -> jdbc.update("INSERT INTO companies (name) VALUES ('   ')"))
                .isInstanceOf(DataIntegrityViolationException.class);
        assertThatThrownBy(() -> jdbc.update(
                        "INSERT INTO companies (name, notes) VALUES ('Oversized Notes Company', repeat('n', 4001))"))
                .isInstanceOf(DataIntegrityViolationException.class);

        UUID companyId = UUID.fromString("47000000-0000-4000-8000-000000000001");
        UUID requestId = UUID.fromString("48000000-0000-4000-8000-000000000001");
        UUID skillId = jdbc.queryForObject(
                "SELECT id FROM skills WHERE skill_status = 'ACTIVE' ORDER BY skill_name LIMIT 1", UUID.class);
        jdbc.update("INSERT INTO companies (id, name) VALUES (?, 'Constraint Test Company')", companyId);

        assertThatThrownBy(() -> jdbc.update(
                        "INSERT INTO internship_requests (company_id, title) VALUES (?, '   ')", companyId))
                .isInstanceOf(DataIntegrityViolationException.class);
        assertThatThrownBy(() -> jdbc.update(
                        "INSERT INTO internship_requests (company_id, title, shortlist_guidance_value) "
                                + "VALUES (?, 'Invalid Guidance', -1)",
                        companyId))
                .isInstanceOf(DataIntegrityViolationException.class);
        assertThatThrownBy(() -> jdbc.update(
                        "INSERT INTO internship_requests (company_id, title, shortlist_guidance_value) "
                                + "VALUES (?, 'Invalid Guidance', 10001)",
                        companyId))
                .isInstanceOf(DataIntegrityViolationException.class);
        assertThatThrownBy(() -> jdbc.update(
                        "INSERT INTO internship_requests (company_id, title, description) "
                                + "VALUES (?, 'Oversized Description', repeat('d', 10001))",
                        companyId))
                .isInstanceOf(DataIntegrityViolationException.class);

        jdbc.update(
                "INSERT INTO internship_requests (id, company_id, title, shortlist_guidance_value) "
                        + "VALUES (?, ?, 'Valid Guidance', 25)",
                requestId,
                companyId);
        jdbc.update(
                "INSERT INTO internship_request_skills (internship_request_id, skill_id) VALUES (?, ?)",
                requestId,
                skillId);

        assertThatThrownBy(() -> jdbc.update(
                        "INSERT INTO internship_request_skills (internship_request_id, skill_id) VALUES (?, ?)",
                        requestId,
                        skillId))
                .isInstanceOf(DataIntegrityViolationException.class);
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

    private boolean columnExists(String schema, String table, String column) {
        try (Connection connection = dataSource.getConnection();
                ResultSet resultSet = connection.getMetaData().getColumns(null, schema, table, column)) {
            return resultSet.next();
        } catch (SQLException exception) {
            throw new IllegalStateException("Failed to inspect migrated PostgreSQL columns", exception);
        }
    }
}
