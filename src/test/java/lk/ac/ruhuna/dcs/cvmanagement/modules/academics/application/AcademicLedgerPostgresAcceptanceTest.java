package lk.ac.ruhuna.dcs.cvmanagement.modules.academics.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.time.OffsetDateTime;
import java.util.HexFormat;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import lk.ac.ruhuna.dcs.cvmanagement.infrastructure.storage.FileStoragePort;
import lk.ac.ruhuna.dcs.cvmanagement.modules.academics.api.dto.response.AcademicLedgerCommitResponse;
import lk.ac.ruhuna.dcs.cvmanagement.modules.academics.api.dto.response.AcademicLedgerUploadDetailResponse;
import lk.ac.ruhuna.dcs.cvmanagement.modules.academics.api.error.AcademicLedgerApiException;
import lk.ac.ruhuna.dcs.cvmanagement.modules.academics.domain.AcademicLedgerUploadStatus;
import lk.ac.ruhuna.dcs.cvmanagement.shared.security.CurrentActor;
import lk.ac.ruhuna.dcs.cvmanagement.shared.security.RoleName;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
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

/** PostgreSQL acceptance tests for the Academic Ledger consistency and recovery boundaries. */
@SpringBootTest
@ActiveProfiles("test")
@Testcontainers(disabledWithoutDocker = true)
class AcademicLedgerPostgresAcceptanceTest {

    private static final UUID ADMIN_ID = UUID.fromString("90000000-0000-4000-8000-000000000001");
    private static final UUID STUDENT_ID = UUID.fromString("90000000-0000-4000-8000-000000000002");
    private static final UUID SUBJECT_ONE_ID = UUID.fromString("90000000-0000-4000-8000-000000000003");
    private static final UUID SUBJECT_TWO_ID = UUID.fromString("90000000-0000-4000-8000-000000000004");
    private static final Path STORAGE_ROOT = Path.of(
            System.getProperty("java.io.tmpdir"), "cv-academic-ledger-postgres-acceptance");

    private static final byte[] VALID_CSV = ("student_index_number,course_code,credits,letter_grade,semester,"
                    + "academic_year,attempt_number,result_status\n"
                    + "SC/2025/09999,CSC2113,3.0,A,Semester 1,2025/2026,1,PASSED\n")
            .getBytes(StandardCharsets.UTF_8);

    @Container
    private static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:16-alpine")
            .withDatabaseName("cv_management_academic_acceptance")
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
    @Autowired private AcademicLedgerCommitService commitService;
    @Autowired private AcademicLedgerUploadService uploadService;
    @Autowired private AcademicLedgerProcessingStateService processingStateService;
    @Autowired private AcademicLedgerProcessingService processingService;
    @Autowired private FileStoragePort fileStorage;

    @BeforeEach
    void seedReferences() {
        clearAcceptanceData();
        jdbc.update(
                "INSERT INTO public.user_accounts(id, university_email, account_status) VALUES (?, ?, 'ACTIVE')",
                ADMIN_ID,
                "ledger.acceptance.admin@dcs.ruh.ac.lk");
        jdbc.update(
                """
                INSERT INTO public.eligible_students(
                    id, index_number, university_email, full_name, academic_level, is_active
                ) VALUES (?, 'SC/2025/09999', 'ledger.acceptance.student@dcs.ruh.ac.lk',
                    'Ledger Acceptance Student', 2, TRUE)
                """,
                STUDENT_ID);
        insertSubject(SUBJECT_ONE_ID, "CSC2113", "Data Communication and Computer Networks");
        insertSubject(SUBJECT_TWO_ID, "CSC2123", "Object Oriented Programming");
    }

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void forcedMidCommitFailureRollsBackOfficialRowsAndRestoresReadyState() {
        UUID existingUpload = createUpload("existing.csv", "existing", "COMMITTED", 1, 1);
        insertOfficialGrade(existingUpload, SUBJECT_TWO_ID, "CSC2123");

        UUID attemptedUpload = createUpload("rollback.csv", "rollback", "READY_TO_COMMIT", 2, 2);
        insertValidStagingRow(attemptedUpload, 2, SUBJECT_ONE_ID, "CSC2113");
        insertValidStagingRow(attemptedUpload, 3, SUBJECT_TWO_ID, "CSC2123");
        authenticateAdmin();

        assertThatThrownBy(() -> commitService.commit(attemptedUpload))
                .isInstanceOfSatisfying(AcademicLedgerApiException.class, exception -> {
                    assertThat(exception.status()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
                    assertThat(exception.code()).isEqualTo("LEDGER_COMMIT_FAILED");
                });

        assertThat(count("academic.official_student_grade", "academic_ledger_upload_id", attemptedUpload))
                .isZero();
        assertThat(count("academic.student_academic_summary", "student_id", STUDENT_ID)).isZero();
        assertThat(uploadStatus(attemptedUpload)).isEqualTo("READY_TO_COMMIT");
        assertThat(jdbc.queryForObject(
                        "SELECT COUNT(*) FROM audit_events WHERE resource_id = ? AND event_type = 'LEDGER_COMMIT_SUCCEEDED'",
                        Integer.class,
                        attemptedUpload.toString()))
                .isZero();
    }

    @Test
    void concurrentCommitAllowsExactlyOneSuccess() throws Exception {
        UUID uploadId = createUpload("concurrent.csv", "concurrent", "READY_TO_COMMIT", 1, 1);
        insertValidStagingRow(uploadId, 2, SUBJECT_ONE_ID, "CSC2113");
        CyclicBarrier start = new CyclicBarrier(2);

        try (ExecutorService executor = Executors.newFixedThreadPool(2)) {
            Callable<Object> commit = () -> {
                authenticateAdmin();
                start.await(10, TimeUnit.SECONDS);
                try {
                    return commitService.commit(uploadId);
                } catch (RuntimeException exception) {
                    return exception;
                } finally {
                    SecurityContextHolder.clearContext();
                }
            };

            Future<Object> first = executor.submit(commit);
            Future<Object> second = executor.submit(commit);
            List<Object> results = List.of(first.get(30, TimeUnit.SECONDS), second.get(30, TimeUnit.SECONDS));

            assertThat(results).filteredOn(AcademicLedgerCommitResponse.class::isInstance).hasSize(1);
            assertThat(results).filteredOn(AcademicLedgerApiException.class::isInstance).singleElement()
                    .satisfies(result -> {
                        AcademicLedgerApiException exception = (AcademicLedgerApiException) result;
                        assertThat(exception.status()).isEqualTo(HttpStatus.CONFLICT);
                        assertThat(exception.code())
                                .isIn("LEDGER_COMMIT_CONFLICT", "LEDGER_ALREADY_COMMITTED");
                    });
        }

        assertThat(uploadStatus(uploadId)).isEqualTo("COMMITTED");
        assertThat(count("academic.official_student_grade", "academic_ledger_upload_id", uploadId))
                .isEqualTo(1);
        assertThat(count("academic.student_academic_summary", "student_id", STUDENT_ID)).isEqualTo(1);
    }

    @Test
    void staleProcessingIsRecoveredAndReplayedFromPersistedSource() throws Exception {
        String storageKey = "academic-ledger/acceptance/recovery-" + UUID.randomUUID() + ".csv";
        FileStoragePort.StoredFile stored = fileStorage.store(
                storageKey, new java.io.ByteArrayInputStream(VALID_CSV));
        UUID fileId = UUID.randomUUID();
        UUID uploadId = UUID.randomUUID();
        insertFileAsset(fileId, "recovery.csv", storageKey, stored.checksumSha256(), stored.sizeBytes());
        insertUpload(uploadId, fileId, "recovery.csv", stored.checksumSha256(), "PROCESSING", 1, 0);
        insertValidStagingRow(uploadId, 2, SUBJECT_TWO_ID, "CSC2123");
        jdbc.update(
                """
                UPDATE academic.academic_ledger_upload
                SET processing_started_at = NOW() - INTERVAL '1 hour',
                    updated_at = NOW() - INTERVAL '1 hour'
                WHERE academic_ledger_upload_id = ?
                """,
                uploadId);

        assertThat(processingStateService.recoverOneStaleProcessing(OffsetDateTime.now())).isTrue();
        assertThat(uploadStatus(uploadId)).isEqualTo("RECEIVED");
        assertThat(count("academic.academic_ledger_staging_row", "academic_ledger_upload_id", uploadId))
                .isZero();

        var recoveredJob = processingStateService.claimNextReceived().orElseThrow();
        assertThat(recoveredJob.uploadId()).isEqualTo(uploadId);
        processingService.process(recoveredJob);

        assertThat(uploadStatus(uploadId)).isEqualTo("READY_TO_COMMIT");
        assertThat(count("academic.academic_ledger_staging_row", "academic_ledger_upload_id", uploadId))
                .isEqualTo(1);
        assertThat(jdbc.queryForObject(
                        "SELECT valid_rows FROM academic.academic_ledger_upload WHERE academic_ledger_upload_id = ?",
                        Integer.class,
                        uploadId))
                .isEqualTo(1);
    }

    @Test
    void duplicateUploadRaceAcceptsOneBatchAndRejectsTheOther() throws Exception {
        CyclicBarrier start = new CyclicBarrier(2);
        try (ExecutorService executor = Executors.newFixedThreadPool(2)) {
            Callable<Object> upload = () -> {
                authenticateAdmin();
                start.await(10, TimeUnit.SECONDS);
                try {
                    return uploadService.upload(new MockMultipartFile(
                            "file", "race.csv", "text/csv", VALID_CSV));
                } catch (RuntimeException exception) {
                    return exception;
                } finally {
                    SecurityContextHolder.clearContext();
                }
            };

            Future<Object> first = executor.submit(upload);
            Future<Object> second = executor.submit(upload);
            List<Object> results = List.of(first.get(30, TimeUnit.SECONDS), second.get(30, TimeUnit.SECONDS));

            assertThat(results).filteredOn(AcademicLedgerUploadDetailResponse.class::isInstance).hasSize(1);
            assertThat(results).filteredOn(AcademicLedgerApiException.class::isInstance).singleElement()
                    .satisfies(result -> {
                        AcademicLedgerApiException exception = (AcademicLedgerApiException) result;
                        assertThat(exception.status()).isEqualTo(HttpStatus.CONFLICT);
                        assertThat(exception.code()).isEqualTo("LEDGER_DUPLICATE_UPLOAD");
                    });
        }

        String checksum = sha256(VALID_CSV);
        assertThat(jdbc.queryForObject(
                        "SELECT COUNT(*) FROM academic.academic_ledger_upload WHERE file_hash = ?",
                        Integer.class,
                        checksum))
                .isEqualTo(1);
        assertThat(jdbc.queryForObject(
                        "SELECT COUNT(*) FROM system.file_asset WHERE checksum_sha256 = ?",
                        Integer.class,
                        checksum))
                .isEqualTo(1);
    }

    private void clearAcceptanceData() {
        jdbc.update("DELETE FROM academic.academic_ledger_validation_error");
        jdbc.update("DELETE FROM academic.academic_ledger_staging_row");
        jdbc.update("DELETE FROM academic.student_academic_summary");
        jdbc.update("DELETE FROM academic.official_student_grade");
        jdbc.update("DELETE FROM academic.academic_ledger_upload");
        jdbc.update("DELETE FROM academic.subject");
        jdbc.update("DELETE FROM system.file_asset");
        jdbc.update("DELETE FROM audit_events WHERE actor_user_id = ?", ADMIN_ID);
        jdbc.update("DELETE FROM public.eligible_students WHERE id = ?", STUDENT_ID);
        jdbc.update("DELETE FROM public.user_accounts WHERE id = ?", ADMIN_ID);
    }

    private void insertSubject(UUID id, String code, String title) {
        jdbc.update(
                """
                INSERT INTO academic.subject(
                    subject_id, catalog_version, cohort_start_year, course_code, course_title,
                    credits, academic_level, semester, course_type, is_active
                ) VALUES (?, 'ACCEPTANCE-TEST', 2025, ?, ?, 3.0, 2, 'Semester 1', 'CORE', TRUE)
                """,
                id,
                code,
                title);
    }

    private UUID createUpload(String fileName, String salt, String status, int totalRows, int validRows) {
        UUID fileId = UUID.randomUUID();
        UUID uploadId = UUID.randomUUID();
        String checksum = sha256(salt.getBytes(StandardCharsets.UTF_8));
        insertFileAsset(fileId, fileName, "academic-ledger/acceptance/" + fileId + ".csv", checksum, 1);
        insertUpload(uploadId, fileId, fileName, checksum, status, totalRows, validRows);
        return uploadId;
    }

    private void insertFileAsset(
            UUID fileId, String fileName, String storageKey, String checksum, long sizeBytes) {
        jdbc.update(
                """
                INSERT INTO system.file_asset(
                    file_asset_id, owner_account_id, file_name, storage_key,
                    mime_type, file_size_bytes, checksum_sha256
                ) VALUES (?, ?, ?, ?, 'text/csv', ?, ?)
                """,
                fileId,
                ADMIN_ID,
                fileName,
                storageKey,
                sizeBytes,
                checksum);
    }

    private void insertUpload(
            UUID uploadId,
            UUID fileId,
            String fileName,
            String checksum,
            String status,
            int totalRows,
            int validRows) {
        OffsetDateTime committedAt = "COMMITTED".equals(status) ? OffsetDateTime.now() : null;
        jdbc.update(
                """
                INSERT INTO academic.academic_ledger_upload(
                    academic_ledger_upload_id, uploaded_by_account_id, source_file_asset_id,
                    file_name, file_hash, upload_status, validation_status,
                    total_rows, valid_rows, invalid_rows, committed_at
                ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, 0, ?)
                """,
                uploadId,
                ADMIN_ID,
                fileId,
                fileName,
                checksum,
                status,
                "READY_TO_COMMIT".equals(status) || "COMMITTED".equals(status) ? "PASSED" : "NOT_STARTED",
                totalRows,
                validRows,
                committedAt);
    }

    private void insertValidStagingRow(UUID uploadId, int rowNumber, UUID subjectId, String courseCode) {
        String title = subjectId.equals(SUBJECT_ONE_ID)
                ? "Data Communication and Computer Networks"
                : "Object Oriented Programming";
        jdbc.update(
                """
                INSERT INTO academic.academic_ledger_staging_row(
                    academic_ledger_upload_id, row_number, raw_payload, student_index_number,
                    student_id, course_code, course_title, credits, letter_grade, grade_point,
                    semester, academic_year, attempt_number, result_status, validation_status
                ) VALUES (?, ?, '{}'::jsonb, 'SC/2025/09999', ?, ?, ?, 3.0, 'A', 4.00,
                    'Semester 1', '2025/2026', 1, 'PASSED', 'VALID')
                """,
                uploadId,
                rowNumber,
                STUDENT_ID,
                courseCode,
                title);
    }

    private void insertOfficialGrade(UUID uploadId, UUID subjectId, String courseCode) {
        jdbc.update(
                """
                INSERT INTO academic.official_student_grade(
                    student_id, subject_id, academic_ledger_upload_id, semester, academic_year,
                    attempt_number, credits, grade_point, letter_grade, result_status, committed_at
                ) VALUES (?, ?, ?, 'Semester 1', '2025/2026', 1, 3.0, 3.00, 'B', 'PASSED', NOW())
                """,
                STUDENT_ID,
                subjectId,
                uploadId);
        assertThat(courseCode).startsWith("CSC");
    }

    private int count(String table, String column, UUID id) {
        return jdbc.queryForObject(
                "SELECT COUNT(*) FROM " + table + " WHERE " + column + " = ?",
                Integer.class,
                id);
    }

    private String uploadStatus(UUID uploadId) {
        return jdbc.queryForObject(
                "SELECT upload_status FROM academic.academic_ledger_upload WHERE academic_ledger_upload_id = ?",
                String.class,
                uploadId);
    }

    private void authenticateAdmin() {
        CurrentActor actor = new CurrentActor(
                ADMIN_ID, "ledger.acceptance.admin@dcs.ruh.ac.lk", Set.of(RoleName.ADMIN));
        SecurityContextHolder.getContext().setAuthentication(new TestingAuthenticationToken(actor, null));
    }

    private String sha256(byte[] bytes) {
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(bytes));
        } catch (java.security.NoSuchAlgorithmException exception) {
            throw new IllegalStateException(exception);
        }
    }
}
