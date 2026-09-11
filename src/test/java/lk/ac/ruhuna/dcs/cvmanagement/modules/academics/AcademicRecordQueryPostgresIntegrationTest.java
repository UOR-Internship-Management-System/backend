package lk.ac.ruhuna.dcs.cvmanagement.modules.academics;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.UUID;
import lk.ac.ruhuna.dcs.cvmanagement.modules.academics.domain.AcademicRecordSort;
import lk.ac.ruhuna.dcs.cvmanagement.modules.academics.persistence.AcademicRecordQueryRepository;
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

/** PostgreSQL contract test for committed-only paging, search, filters, and stable sorting. */
@SpringBootTest
@ActiveProfiles("test")
@Testcontainers(disabledWithoutDocker = true)
class AcademicRecordQueryPostgresIntegrationTest {

    @Container
    private static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:16-alpine")
            .withDatabaseName("cv_management_academic_query_test")
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
    @Autowired private AcademicRecordQueryRepository repository;

    private final UUID studentOne = UUID.fromString("81000000-0000-4000-8000-000000000001");
    private final UUID studentTwo = UUID.fromString("81000000-0000-4000-8000-000000000002");

    @BeforeEach
    void seedCommittedRecords() {
        jdbcTemplate.update("DELETE FROM academic.official_student_grade");
        jdbcTemplate.update("DELETE FROM academic.academic_ledger_upload");
        jdbcTemplate.update("DELETE FROM system.file_asset");
        jdbcTemplate.update("DELETE FROM academic.subject");
        jdbcTemplate.update("DELETE FROM public.eligible_students WHERE id IN (?, ?)", studentOne, studentTwo);
        jdbcTemplate.update("DELETE FROM public.user_accounts WHERE id = ?", UUID.fromString("82000000-0000-4000-8000-000000000001"));

        UUID account = UUID.fromString("82000000-0000-4000-8000-000000000001");
        jdbcTemplate.update("INSERT INTO public.user_accounts(id, university_email, account_status) VALUES (?, ?, 'ACTIVE')",
                account, "academic.query.admin@dcs.ruh.ac.lk");
        jdbcTemplate.update("INSERT INTO public.eligible_students(id, index_number, university_email, full_name, academic_level, is_active) VALUES (?, ?, ?, ?, 3, TRUE)",
                studentOne, "SC/2025/09001", "sc202509001@dcs.ruh.ac.lk", "Student One");
        jdbcTemplate.update("INSERT INTO public.eligible_students(id, index_number, university_email, full_name, academic_level, is_active) VALUES (?, ?, ?, ?, 3, TRUE)",
                studentTwo, "SC/2025/09002", "sc202509002@dcs.ruh.ac.lk", "Student Two");

        UUID subjectA = UUID.fromString("83000000-0000-4000-8000-000000000001");
        UUID subjectB = UUID.fromString("83000000-0000-4000-8000-000000000002");
        jdbcTemplate.update("INSERT INTO academic.subject(subject_id, catalog_version, cohort_start_year, course_code, course_title, credits, academic_level, semester, course_type, is_active) VALUES (?, 'SC2025', 2025, 'CSC2113', 'Data Communication and Computer Networks', 3.0, 2, 'Semester 1', 'CORE', TRUE)", subjectA);
        jdbcTemplate.update("INSERT INTO academic.subject(subject_id, catalog_version, cohort_start_year, course_code, course_title, credits, academic_level, semester, course_type, is_active) VALUES (?, 'SC2025', 2025, 'CSC2123', 'Object Oriented Programming', 3.0, 2, 'Semester 1', 'CORE', TRUE)", subjectB);

        UUID file = UUID.fromString("84000000-0000-4000-8000-000000000001");
        UUID upload = UUID.fromString("85000000-0000-4000-8000-000000000001");
        jdbcTemplate.update("INSERT INTO system.file_asset(file_asset_id, owner_account_id, file_name, storage_key, mime_type, file_size_bytes, checksum_sha256) VALUES (?, ?, 'query.csv', 'academic-ledger/query.csv', 'text/csv', 10, ?)", file, account, "a".repeat(64));
        jdbcTemplate.update("INSERT INTO academic.academic_ledger_upload(academic_ledger_upload_id, uploaded_by_account_id, source_file_asset_id, file_name, file_hash, upload_status, validation_status, total_rows, valid_rows, invalid_rows, committed_at) VALUES (?, ?, ?, 'query.csv', ?, 'COMMITTED', 'PASSED', 3, 3, 0, '2026-08-13T02:00:00Z')",
                upload, account, file, "a".repeat(64));

        insertGrade(UUID.fromString("86000000-0000-4000-8000-000000000001"), studentOne, subjectA, upload, "2025/2026", "A-", "3.70");
        insertGrade(UUID.fromString("86000000-0000-4000-8000-000000000002"), studentOne, subjectB, upload, "2024/2025", "B+", "3.30");
        insertGrade(UUID.fromString("86000000-0000-4000-8000-000000000003"), studentTwo, subjectA, upload, "2025/2026", "A", "4.00");
    }

    @Test
    void searchesFiltersAndPaginatesOnlyOfficialRecords() {
        var bySearch = repository.search("network", null, null, 0, 20, AcademicRecordSort.ACADEMIC_YEAR_DESC);
        assertThat(bySearch.getTotalElements()).isEqualTo(2);
        assertThat(bySearch.getContent()).allMatch(record -> record.courseCode().equals("CSC2113"));

        var byStudentAndCourse = repository.search(null, "CSC2113", studentOne, 0, 20, AcademicRecordSort.GRADE_POINT_DESC);
        assertThat(byStudentAndCourse.getTotalElements()).isEqualTo(1);
        assertThat(byStudentAndCourse.getContent().getFirst().gradePoint()).isEqualByComparingTo("3.70");

        var firstPage = repository.search(null, null, null, 0, 1, AcademicRecordSort.ACADEMIC_YEAR_DESC);
        assertThat(firstPage.getSize()).isEqualTo(1);
        assertThat(firstPage.getTotalElements()).isEqualTo(3);
        assertThat(firstPage.getTotalPages()).isEqualTo(3);
    }

    private void insertGrade(UUID id, UUID studentId, UUID subjectId, UUID uploadId, String academicYear, String grade, String point) {
        jdbcTemplate.update("INSERT INTO academic.official_student_grade(official_student_grade_id, student_id, subject_id, academic_ledger_upload_id, semester, academic_year, attempt_number, credits, grade_point, letter_grade, result_status, committed_at) VALUES (?, ?, ?, ?, 'Semester 1', ?, 1, 3.0, CAST(? AS NUMERIC), ?, 'PASSED', '2026-08-13T02:00:00Z')",
                id, studentId, subjectId, uploadId, academicYear, point, grade);
    }
}
