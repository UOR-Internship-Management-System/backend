package lk.ac.ruhuna.dcs.cvmanagement.modules.academics.application;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.Set;
import java.util.UUID;
import lk.ac.ruhuna.dcs.cvmanagement.modules.academics.domain.AcademicLedgerUploadStatus;
import lk.ac.ruhuna.dcs.cvmanagement.shared.security.CurrentActor;
import lk.ac.ruhuna.dcs.cvmanagement.shared.security.RoleName;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

/** Proves that the authoritative CSC catalogue drives validation, commit, and the CSC-only GPA read model. */
@SpringBootTest
@ActiveProfiles("test")
@Testcontainers(disabledWithoutDocker = true)
class AcademicSubjectCataloguePostgresIntegrationTest {

    private static final UUID ADMIN_ID = UUID.fromString("96000000-0000-4000-8000-000000000001");
    private static final UUID STUDENT_ID = UUID.fromString("96000000-0000-4000-8000-000000000002");
    private static final String STUDENT_INDEX = "SC/2022/12865";
    private static final Path STORAGE_ROOT = Path.of(
            System.getProperty("java.io.tmpdir"), "cv-authoritative-catalogue-integration");

    @Container
    private static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:16-alpine")
            .withDatabaseName("cv_management_catalogue_test")
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
        registry.add("app.academics.storage.root", () -> STORAGE_ROOT.toString());
        registry.add("app.academics.ledger.processing.worker-enabled", () -> false);
    }

    @Autowired private JdbcTemplate jdbc;
    @Autowired private AcademicLedgerUploadService uploadService;
    @Autowired private AcademicLedgerProcessingStateService processingStateService;
    @Autowired private AcademicLedgerProcessingService processingService;
    @Autowired private AcademicLedgerCommitService commitService;

    @BeforeEach
    void seedActors() {
        clearOperationalData();
        jdbc.update(
                "INSERT INTO public.user_accounts(id, university_email, account_status) VALUES (?, ?, 'ACTIVE')",
                ADMIN_ID,
                "catalogue.test.admin@dcs.ruh.ac.lk");
        jdbc.update(
                """
                INSERT INTO public.eligible_students(
                    id, index_number, university_email, full_name, academic_level, is_active
                ) VALUES (?, ?, 'catalogue.test.student@dcs.ruh.ac.lk', 'Catalogue Test Student', 3, TRUE)
                """,
                STUDENT_ID,
                STUDENT_INDEX);
        authenticateAdmin();
    }

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void seededCsc1213FlowsThroughCommitAndPopulatesTheCscOnlyGpaSummary() {
        UUID uploadId = uploadAndProcess("""
                student_index_number,course_code,credits,letter_grade,semester,academic_year,attempt_number,result_status
                SC/2022/12865,CSC1213,3.0,A,Semester 2,2022/2023,1,PASSED
                """);

        assertThat(uploadStatus(uploadId)).isEqualTo(AcademicLedgerUploadStatus.READY_TO_COMMIT.name());
        var result = commitService.commit(uploadId);

        assertThat(result.status()).isEqualTo(AcademicLedgerUploadStatus.COMMITTED.name());
        assertThat(result.committedRecords()).isEqualTo(1);
        assertThat(result.affectedStudents()).isEqualTo(1);
        assertThat(result.recalculatedGpaCount()).isEqualTo(1);
        assertThat(jdbc.queryForObject(
                        "SELECT credits FROM academic.official_student_grade WHERE academic_ledger_upload_id = ?",
                        BigDecimal.class,
                        uploadId))
                .isEqualByComparingTo("3.0");
        assertThat(jdbc.queryForObject(
                        "SELECT computer_science_gpa FROM academic.student_academic_summary WHERE student_id = ?",
                        BigDecimal.class,
                        STUDENT_ID))
                .isEqualByComparingTo("4.00");
        assertThat(jdbc.queryForObject(
                        "SELECT total_credits FROM academic.student_academic_summary WHERE student_id = ?",
                        BigDecimal.class,
                        STUDENT_ID))
                .isEqualByComparingTo("3.0");
    }

    @Test
    void rejectsCreditsThatDoNotMatchTheConfirmedCsc1213Value() {
        UUID uploadId = uploadAndProcess("""
                student_index_number,course_code,credits,letter_grade,semester,academic_year,attempt_number,result_status
                SC/2022/12865,CSC1213,2.0,A,Semester 2,2022/2023,1,PASSED
                """);

        assertThat(uploadStatus(uploadId)).isEqualTo(AcademicLedgerUploadStatus.VALIDATION_FAILED.name());
        assertThat(validationCodes(uploadId)).containsExactly("CREDITS_MISMATCH");
        assertThat(countOfficialRows(uploadId)).isZero();
    }

    @Test
    void acceptsEStarOnlyAsAbsentAndRejectsUnapprovedStatuses() {
        UUID absentUpload = uploadAndProcess("""
                student_index_number,course_code,credits,letter_grade,semester,academic_year,attempt_number,result_status
                SC/2022/12865,CSC1122,2.0,E*,Semester 1,2022/2023,1,ABSENT
                """);
        assertThat(uploadStatus(absentUpload)).isEqualTo(AcademicLedgerUploadStatus.READY_TO_COMMIT.name());

        UUID withheldUpload = uploadAndProcess("""
                student_index_number,course_code,credits,letter_grade,semester,academic_year,attempt_number,result_status
                SC/2022/12865,CSC1113,3.0,A,Semester 1,2022/2023,1,WITHHELD
                """);
        assertThat(uploadStatus(withheldUpload)).isEqualTo(AcademicLedgerUploadStatus.VALIDATION_FAILED.name());
        assertThat(validationCodes(withheldUpload)).containsExactly("RESULT_STATUS_INVALID");
    }

    private UUID uploadAndProcess(String csv) {
        byte[] content = csv.getBytes(StandardCharsets.UTF_8);
        var accepted = uploadService.upload(new MockMultipartFile(
                "file", "catalogue-" + UUID.randomUUID() + ".csv", "text/csv", content));
        var job = processingStateService.claimNextReceived().orElseThrow();
        assertThat(job.uploadId()).isEqualTo(accepted.uploadId());
        processingService.process(job);
        return accepted.uploadId();
    }

    private Set<String> validationCodes(UUID uploadId) {
        return Set.copyOf(jdbc.queryForList(
                """
                SELECT e.error_code
                FROM academic.academic_ledger_validation_error e
                JOIN academic.academic_ledger_staging_row r
                  ON r.staging_row_id = e.staging_row_id
                WHERE r.academic_ledger_upload_id = ?
                """,
                String.class,
                uploadId));
    }

    private int countOfficialRows(UUID uploadId) {
        return jdbc.queryForObject(
                "SELECT COUNT(*) FROM academic.official_student_grade WHERE academic_ledger_upload_id = ?",
                Integer.class,
                uploadId);
    }

    private String uploadStatus(UUID uploadId) {
        return jdbc.queryForObject(
                "SELECT upload_status FROM academic.academic_ledger_upload WHERE academic_ledger_upload_id = ?",
                String.class,
                uploadId);
    }

    private void clearOperationalData() {
        jdbc.update("DELETE FROM academic.academic_ledger_validation_error");
        jdbc.update("DELETE FROM academic.academic_ledger_staging_row");
        jdbc.update("DELETE FROM academic.student_academic_summary");
        jdbc.update("DELETE FROM academic.official_student_grade");
        jdbc.update("DELETE FROM academic.academic_ledger_upload");
        jdbc.update("DELETE FROM system.file_asset");
        jdbc.update("DELETE FROM audit_events WHERE actor_user_id = ?", ADMIN_ID);
        jdbc.update("DELETE FROM public.eligible_students WHERE id = ?", STUDENT_ID);
        jdbc.update("DELETE FROM public.user_accounts WHERE id = ?", ADMIN_ID);
    }

    private void authenticateAdmin() {
        CurrentActor actor = new CurrentActor(
                ADMIN_ID, "catalogue.test.admin@dcs.ruh.ac.lk", Set.of(RoleName.ADMIN));
        SecurityContextHolder.getContext().setAuthentication(new TestingAuthenticationToken(actor, null));
    }
}
