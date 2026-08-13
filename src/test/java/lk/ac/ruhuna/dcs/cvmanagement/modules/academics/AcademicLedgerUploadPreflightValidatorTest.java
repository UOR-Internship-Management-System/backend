package lk.ac.ruhuna.dcs.cvmanagement.modules.academics;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.HexFormat;
import lk.ac.ruhuna.dcs.cvmanagement.modules.academics.api.error.AcademicLedgerApiException;
import lk.ac.ruhuna.dcs.cvmanagement.modules.academics.application.AcademicLedgerUploadPreflightValidator;
import lk.ac.ruhuna.dcs.cvmanagement.modules.academics.config.AcademicLedgerProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;

class AcademicLedgerUploadPreflightValidatorTest {

    private static final long MAX_BYTES = 5_242_880L;
    private AcademicLedgerUploadPreflightValidator validator;

    @BeforeEach
    void setUp() {
        validator = new AcademicLedgerUploadPreflightValidator(new AcademicLedgerProperties(MAX_BYTES, 2));
    }

    @Test
    void validCanonicalCsvPassesPreflightAndProducesContentDigest() throws Exception {
        byte[] bytes = validCsv().getBytes(StandardCharsets.UTF_8);
        MockMultipartFile file = new MockMultipartFile(
                "file", "ledger.csv", "text/csv", bytes);

        var result = validator.validate(file);

        assertThat(result.originalFilename()).isEqualTo("ledger.csv");
        assertThat(result.contentType()).isEqualTo("text/csv");
        assertThat(result.sizeBytes()).isEqualTo(bytes.length);
        assertThat(result.checksumSha256()).isEqualTo(HexFormat.of().formatHex(
                MessageDigest.getInstance("SHA-256").digest(bytes)));
    }

    @Test
    void stripsClientSidePathFromOriginalFilename() {
        MockMultipartFile file = new MockMultipartFile(
                "file", "C:\\fakepath\\ledger.csv", "text/csv", validCsv().getBytes(StandardCharsets.UTF_8));

        assertThat(validator.validate(file).originalFilename()).isEqualTo("ledger.csv");
    }

    @Test
    void acceptsAValidFileExactlyAtTheConfiguredSizeBoundary() {
        byte[] bytes = validCsv().getBytes(StandardCharsets.UTF_8);
        var boundaryValidator = new AcademicLedgerUploadPreflightValidator(
                new AcademicLedgerProperties(bytes.length, 2));
        MockMultipartFile file = new MockMultipartFile("file", "ledger.csv", "text/csv", bytes);

        assertThat(boundaryValidator.validate(file).sizeBytes()).isEqualTo(bytes.length);
    }

    @Test
    void rejectsWrongFileExtension() {
        MockMultipartFile file = new MockMultipartFile(
                "file", "ledger.txt", "text/csv", validCsv().getBytes(StandardCharsets.UTF_8));

        assertLedgerError(file, 415, "LEDGER_MEDIA_TYPE_UNSUPPORTED");
    }

    @Test
    void rejectsNonCsvMediaType() {
        MockMultipartFile file = new MockMultipartFile(
                "file", "ledger.csv", "application/octet-stream", validCsv().getBytes(StandardCharsets.UTF_8));

        assertLedgerError(file, 415, "LEDGER_MEDIA_TYPE_UNSUPPORTED");
    }

    @Test
    void rejectsWrongHeaderOrder() {
        String csv = "course_code,student_index_number,credits,letter_grade,semester,academic_year,attempt_number,result_status\r\n"
                + "CSC1113,SC/2025/0001,3.0,A,Semester 1,2025/2026,1,PASSED\r\n";
        MockMultipartFile file = new MockMultipartFile("file", "ledger.csv", "text/csv", csv.getBytes(StandardCharsets.UTF_8));

        assertLedgerError(file, 422, "LEDGER_PARSE_FAILED");
    }

    @Test
    void rejectsMalformedUtf8() {
        byte[] header = ("student_index_number,course_code,credits,letter_grade,semester,academic_year,attempt_number,result_status\r\n"
                + "SC/2025/0001,CSC1113,3.0,A,Semester 1,2025/2026,1,")
                .getBytes(StandardCharsets.UTF_8);
        byte[] malformed = new byte[header.length + 3];
        System.arraycopy(header, 0, malformed, 0, header.length);
        malformed[header.length] = (byte) 0xC3;
        malformed[header.length + 1] = (byte) 0x28;
        malformed[header.length + 2] = '\n';
        MockMultipartFile file = new MockMultipartFile("file", "ledger.csv", "text/csv", malformed);

        assertLedgerError(file, 422, "LEDGER_PARSE_FAILED");
    }

    @Test
    void rejectsNonParseablePrimitiveValues() {
        String csv = "student_index_number,course_code,credits,letter_grade,semester,academic_year,attempt_number,result_status\r\n"
                + "SC/2025/0001,CSC1113,three,A,Semester 1,2025/2026,1,PASSED\r\n";
        MockMultipartFile file = new MockMultipartFile("file", "ledger.csv", "text/csv", csv.getBytes(StandardCharsets.UTF_8));

        assertLedgerError(file, 422, "LEDGER_PARSE_FAILED");
    }

    @Test
    void rejectsFileAboveFrozenFiveMibLimit() {
        byte[] bytes = new byte[(int) MAX_BYTES + 1];
        MockMultipartFile file = new MockMultipartFile("file", "ledger.csv", "text/csv", bytes);

        assertLedgerError(file, 413, "LEDGER_FILE_TOO_LARGE");
    }

    private void assertLedgerError(MockMultipartFile file, int status, String code) {
        assertThatThrownBy(() -> validator.validate(file))
                .isInstanceOfSatisfying(AcademicLedgerApiException.class, exception -> {
                    assertThat(exception.status().value()).isEqualTo(status);
                    assertThat(exception.code()).isEqualTo(code);
                });
    }

    private String validCsv() {
        return "student_index_number,course_code,credits,letter_grade,semester,academic_year,attempt_number,result_status\r\n"
                + "SC/2025/0001,CSC1113,3.0,A,Semester 1,2025/2026,1,PASSED\r\n";
    }
}
