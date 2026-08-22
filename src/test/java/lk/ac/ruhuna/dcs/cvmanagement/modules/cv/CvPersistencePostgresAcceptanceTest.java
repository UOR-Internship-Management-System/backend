package lk.ac.ruhuna.dcs.cvmanagement.modules.cv;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

/** PostgreSQL/Flyway acceptance for the BMD-007 durable active-CV and selection model. */
@SpringBootTest
@ActiveProfiles("test")
@Testcontainers(disabledWithoutDocker = true)
class CvPersistencePostgresAcceptanceTest {

    @Container
    private static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:16-alpine")
            .withDatabaseName("cv_management_cv_acceptance")
            .withUsername("cv_user")
            .withPassword("cv_local_password");

    @DynamicPropertySource
    static void postgresProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", POSTGRES::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGRES::getUsername);
        registry.add("spring.datasource.password", POSTGRES::getPassword);
        registry.add("spring.datasource.driver-class-name", POSTGRES::getDriverClassName);
        registry.add("spring.flyway.enabled", () -> true);
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "validate");
        registry.add("app.cv.cleanup.poll-delay-ms", () -> "3600000");
    }

    @Autowired
    private JdbcTemplate jdbc;

    private final UUID studentOne = UUID.fromString("71000000-0000-4000-8000-000000000001");
    private final UUID studentTwo = UUID.fromString("71000000-0000-4000-8000-000000000002");

    @AfterEach
    void cleanup() {
        jdbc.update("DELETE FROM cv_selected_projects WHERE student_id IN (?, ?)", studentOne, studentTwo);
        jdbc.update("DELETE FROM cvs WHERE student_id IN (?, ?)", studentOne, studentTwo);
        jdbc.update("DELETE FROM cv_source_freshness WHERE student_id IN (?, ?)", studentOne, studentTwo);
        jdbc.update("DELETE FROM eligible_students WHERE id IN (?, ?)", studentOne, studentTwo);
    }

    @Test
    void flywayAppliesAtLeastV084AndNormalizedSelectionsEnforceParentStudentIntegrity() {
        Integer version = jdbc.queryForObject(
                "SELECT MAX(version::integer) FROM flyway_schema_history WHERE success", Integer.class);
        assertThat(version).isGreaterThanOrEqualTo(84);
        assertThat(tableExists("cv_previews")).isTrue();
        assertThat(tableExists("cv_selected_projects")).isTrue();
        assertThat(tableExists("cv_preview_projects")).isTrue();

        insertStudent(studentOne, "SC/2026/81001", "cv.acceptance.one@ruh.ac.lk");
        insertStudent(studentTwo, "SC/2026/81002", "cv.acceptance.two@ruh.ac.lk");
        UUID cvId = UUID.randomUUID();
        jdbc.update("""
                INSERT INTO cvs (
                    id, student_id, revision, source_fingerprint, created_at, generated_at, saved_at, updated_at
                ) VALUES (?, ?, 1, ?, NOW(), NOW(), NOW(), NOW())
                """, cvId, studentOne, "a".repeat(64));
        UUID projectId = UUID.randomUUID();
        jdbc.update("INSERT INTO cv_selected_projects (cv_id, student_id, source_record_id) VALUES (?, ?, ?)",
                cvId, studentOne, projectId);

        assertThatThrownBy(() -> jdbc.update(
                "INSERT INTO cv_selected_projects (cv_id, student_id, source_record_id) VALUES (?, ?, ?)",
                cvId, studentOne, projectId))
                .isInstanceOf(DataAccessException.class);

        assertThatThrownBy(() -> jdbc.update(
                "INSERT INTO cv_selected_projects (cv_id, student_id, source_record_id) VALUES (?, ?, ?)",
                cvId, studentTwo, UUID.randomUUID()))
                .isInstanceOf(DataAccessException.class);
    }

    @Test
    void databaseAllowsOnlyOneActiveCvRowPerStudent() {
        insertStudent(studentOne, "SC/2026/81003", "cv.acceptance.unique@ruh.ac.lk");
        jdbc.update("INSERT INTO cvs (id, student_id, revision) VALUES (?, ?, 1)", UUID.randomUUID(), studentOne);

        assertThatThrownBy(() -> jdbc.update(
                "INSERT INTO cvs (id, student_id, revision) VALUES (?, ?, 1)", UUID.randomUUID(), studentOne))
                .isInstanceOf(DataAccessException.class);
    }

    private void insertStudent(UUID id, String indexNumber, String email) {
        jdbc.update("""
                INSERT INTO eligible_students (id, index_number, university_email, full_name, academic_level)
                VALUES (?, ?, ?, 'CV Acceptance Student', 4)
                """, id, indexNumber, email);
    }

    private boolean tableExists(String tableName) {
        Boolean exists = jdbc.queryForObject(
                "SELECT to_regclass('public.' || ?) IS NOT NULL", Boolean.class, tableName);
        return Boolean.TRUE.equals(exists);
    }
}
