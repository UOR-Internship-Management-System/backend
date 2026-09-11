package lk.ac.ruhuna.dcs.cvmanagement.modules.adminstudents;

import static org.assertj.core.api.Assertions.assertThat;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import lk.ac.ruhuna.dcs.cvmanagement.modules.adminstudents.domain.model.AdminAcademicRecordSort;
import lk.ac.ruhuna.dcs.cvmanagement.modules.adminstudents.persistence.query.AdminAcademicRecordReadRepository;
import lk.ac.ruhuna.dcs.cvmanagement.modules.adminstudents.persistence.query.AdminDeclaredSkillReadRepository;
import lk.ac.ruhuna.dcs.cvmanagement.modules.adminstudents.persistence.query.AdminProjectReadRepository;
import lk.ac.ruhuna.dcs.cvmanagement.modules.adminstudents.persistence.query.RegisteredStudentReadRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

/** PostgreSQL contract coverage for Patch 4 read-only child collections. */
@SpringBootTest
@ActiveProfiles("test")
@Testcontainers(disabledWithoutDocker = true)
class AdminStudentChildCollectionsPostgresIntegrationTest {

    private static final UUID STUDENT = UUID.fromString("7a000000-0000-4000-8000-000000000001");
    private static final UUID ACCOUNT = UUID.fromString("7a000000-0000-4000-8000-000000000002");
    private static final UUID DECLARED_REACT = UUID.fromString("7a000000-0000-4000-8000-000000000003");
    private static final UUID DECLARED_SPRING = UUID.fromString("7a000000-0000-4000-8000-000000000004");
    private static final UUID PROJECT_ONE = UUID.fromString("7a000000-0000-4000-8000-000000000005");
    private static final UUID PROJECT_TWO = UUID.fromString("7a000000-0000-4000-8000-000000000006");
    private static final UUID FILE_ASSET = UUID.fromString("7a000000-0000-4000-8000-000000000007");
    private static final UUID LEDGER_UPLOAD = UUID.fromString("7a000000-0000-4000-8000-000000000008");
    private static final UUID SUBJECT = UUID.fromString("7a000000-0000-4000-8000-000000000009");
    private static final UUID GRADE = UUID.fromString("7a000000-0000-4000-8000-00000000000a");

    private static final UUID REACT = UUID.fromString("c0000000-0000-0000-0000-000000000001");
    private static final UUID SPRING_BOOT = UUID.fromString("c0000000-0000-0000-0000-000000000003");

    @Container
    private static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:16-alpine")
            .withDatabaseName("cv_management_admin_students_children_test")
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
    }

    @Autowired private JdbcTemplate jdbcTemplate;
    @Autowired private RegisteredStudentReadRepository registeredStudentRepository;
    @Autowired private AdminDeclaredSkillReadRepository declaredSkillRepository;
    @Autowired private AdminProjectReadRepository projectRepository;
    @Autowired private AdminAcademicRecordReadRepository academicRecordRepository;

    @BeforeEach
    void seed() {
        cleanup();

        jdbcTemplate.update(
                "INSERT INTO public.user_accounts (id, university_email, password_hash, account_status) VALUES (?, ?, 'not-used', 'ACTIVE')",
                ACCOUNT,
                "sc202210099@dcs.ruh.ac.lk");
        jdbcTemplate.update(
                "INSERT INTO public.user_roles (user_id, role_id) SELECT ?, id FROM public.roles WHERE name = 'ROLE_STUDENT'",
                ACCOUNT);
        jdbcTemplate.update(
                """
                INSERT INTO public.eligible_students (
                    id, index_number, university_email, full_name, academic_level,
                    is_active, user_account_id, created_at, updated_at
                ) VALUES (?, 'SC/2022/10099', 'sc202210099@dcs.ruh.ac.lk', 'Patch Four Student', 4,
                          TRUE, ?, '2026-08-14T08:00:00Z', '2026-08-14T08:00:00Z')
                """,
                STUDENT,
                ACCOUNT);

        jdbcTemplate.update(
                """
                INSERT INTO public.student_declared_skills
                    (id, student_id, skill_id, competency_level, version, created_at, updated_at)
                VALUES (?, ?, ?, 'INTERMEDIATE', 1, '2026-08-14T08:00:00Z', '2026-08-14T09:00:00Z'),
                       (?, ?, ?, 'ADVANCED', 2, '2026-08-14T08:00:00Z', '2026-08-14T10:00:00Z')
                """,
                DECLARED_REACT, STUDENT, REACT,
                DECLARED_SPRING, STUDENT, SPRING_BOOT);

        jdbcTemplate.update(
                """
                INSERT INTO public.student_projects
                    (id, student_id, title, description, repository_url, start_date, include_in_cv,
                     version, created_at, updated_at)
                VALUES (?, ?, 'Older Project', 'Batch loading example', 'https://example.com/older',
                        '2026-01-01', TRUE, 1, '2026-08-14T08:00:00Z', '2026-08-14T09:00:00Z'),
                       (?, ?, 'Modern Portal', 'Spring Boot portal', 'https://example.com/portal',
                        '2026-02-01', TRUE, 2, '2026-08-14T08:00:00Z', '2026-08-14T11:00:00Z')
                """,
                PROJECT_ONE, STUDENT,
                PROJECT_TWO, STUDENT);
        jdbcTemplate.update(
                """
                INSERT INTO public.student_project_skills (project_id, skill_id)
                VALUES (?, ?), (?, ?), (?, ?)
                """,
                PROJECT_ONE, REACT,
                PROJECT_TWO, REACT,
                PROJECT_TWO, SPRING_BOOT);

        jdbcTemplate.update(
                """
                INSERT INTO system.file_asset (
                    file_asset_id, owner_account_id, file_name, storage_key, mime_type,
                    file_size_bytes, checksum_sha256, created_at
                ) VALUES (?, ?, 'patch4.csv', 'tests/patch4.csv', 'text/csv', 10,
                          'aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa',
                          '2026-08-14T08:00:00Z')
                """,
                FILE_ASSET,
                ACCOUNT);
        jdbcTemplate.update(
                """
                INSERT INTO academic.academic_ledger_upload (
                    academic_ledger_upload_id, uploaded_by_account_id, source_file_asset_id,
                    file_name, file_hash, upload_status, validation_status, total_rows,
                    valid_rows, invalid_rows, created_at, updated_at, committed_at
                ) VALUES (?, ?, ?, 'patch4.csv',
                          'bbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbb',
                          'COMMITTED', 'PASSED', 1, 1, 0,
                          '2026-08-14T08:00:00Z', '2026-08-14T08:00:00Z', '2026-08-14T08:00:00Z')
                """,
                LEDGER_UPLOAD,
                ACCOUNT,
                FILE_ASSET);
        jdbcTemplate.update(
                """
                INSERT INTO academic.subject (
                    subject_id, catalog_version, cohort_start_year, course_code, course_title,
                    credits, academic_level, semester, course_type
                ) VALUES (?, 'patch4', 2022, 'CSC3999', 'Patch Four Systems', 4.0, 4,
                          'Semester 1', 'CORE')
                """,
                SUBJECT);
        jdbcTemplate.update(
                """
                INSERT INTO academic.official_student_grade (
                    official_student_grade_id, student_id, subject_id, academic_ledger_upload_id,
                    semester, academic_year, attempt_number, credits, grade_point, letter_grade,
                    result_status, committed_at
                ) VALUES (?, ?, ?, ?, 'Semester 1', '2025/2026', 1, 4.0, 4.00, 'A',
                          'PASSED', '2026-08-14T08:00:00Z')
                """,
                GRADE,
                STUDENT,
                SUBJECT,
                LEDGER_UPLOAD);
    }

    @Test
    void queriesSkillsProjectsWithBatchedSkillsAndCommittedAcademicRecords() {
        assertThat(registeredStudentRepository.existsRegisteredStudent(STUDENT)).isTrue();

        var skills = declaredSkillRepository.search(STUDENT, "spring", 0, 20);
        assertThat(skills.getTotalElements()).isEqualTo(1);
        assertThat(skills.getContent()).singleElement().satisfies(skill -> {
            assertThat(skill.skillName()).isEqualTo("Spring Boot");
            assertThat(skill.competencyLevel().name()).isEqualTo("ADVANCED");
        });

        var projects = projectRepository.search(STUDENT, "portal", 0, 20);
        assertThat(projects.getContent()).singleElement()
                .satisfies(project -> assertThat(project.projectId()).isEqualTo(PROJECT_TWO));
        var projectSkills = projectRepository.findSkills(
                STUDENT,
                projects.getContent().stream().map(project -> project.projectId()).toList());
        assertThat(projectSkills).extracting(row -> row.name())
                .containsExactly("React", "Spring Boot");

        var academic = academicRecordRepository.search(
                STUDENT,
                "patch four",
                "CSC3999",
                0,
                20,
                AdminAcademicRecordSort.ACADEMIC_YEAR_DESC);
        assertThat(academic.getContent()).singleElement().satisfies(record -> {
            assertThat(record.academicRecordId()).isEqualTo(GRADE);
            assertThat(record.courseCode()).isEqualTo("CSC3999");
            assertThat(record.gradePoint()).isEqualByComparingTo("4.00");
        });
    }

    @Test
    void searchEscapesSqlWildcardsAndChildQueriesDoNotMutateStudentOwnedRows() {
        long beforeSkills = count("student_declared_skills");
        long beforeProjects = count("student_projects");
        long beforeProjectSkills = count("student_project_skills");
        long beforeGrades = countAcademicGrades();

        assertThat(declaredSkillRepository.search(STUDENT, "%", 0, 20).getTotalElements()).isZero();
        assertThat(projectRepository.search(STUDENT, "_", 0, 20).getTotalElements()).isZero();
        assertThat(academicRecordRepository.search(
                        STUDENT, "%", null, 0, 20, AdminAcademicRecordSort.ACADEMIC_YEAR_DESC)
                .getTotalElements()).isZero();

        assertThat(count("student_declared_skills")).isEqualTo(beforeSkills);
        assertThat(count("student_projects")).isEqualTo(beforeProjects);
        assertThat(count("student_project_skills")).isEqualTo(beforeProjectSkills);
        assertThat(countAcademicGrades()).isEqualTo(beforeGrades);
    }

    private long count(String table) {
        Long count;
        if ("student_project_skills".equals(table)) {
            count = jdbcTemplate.queryForObject(
                    "SELECT COUNT(*) FROM public.student_project_skills WHERE project_id IN (?, ?)",
                    Long.class,
                    PROJECT_ONE,
                    PROJECT_TWO);
        } else {
            count = jdbcTemplate.queryForObject(
                    "SELECT COUNT(*) FROM public." + table + " WHERE student_id = ?",
                    Long.class,
                    STUDENT);
        }
        return count == null ? 0 : count;
    }

    private long countAcademicGrades() {
        Long count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM academic.official_student_grade WHERE student_id = ?",
                Long.class,
                STUDENT);
        return count == null ? 0 : count;
    }

    private void cleanup() {
        jdbcTemplate.update("DELETE FROM academic.official_student_grade WHERE official_student_grade_id = ?", GRADE);
        jdbcTemplate.update("DELETE FROM academic.subject WHERE subject_id = ?", SUBJECT);
        jdbcTemplate.update("DELETE FROM academic.academic_ledger_upload WHERE academic_ledger_upload_id = ?", LEDGER_UPLOAD);
        jdbcTemplate.update("DELETE FROM system.file_asset WHERE file_asset_id = ?", FILE_ASSET);
        jdbcTemplate.update("DELETE FROM public.eligible_students WHERE id = ?", STUDENT);
        jdbcTemplate.update("DELETE FROM public.user_accounts WHERE id = ?", ACCOUNT);
    }
}
