package lk.ac.ruhuna.dcs.cvmanagement.modules.academics;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.HexFormat;
import lk.ac.ruhuna.dcs.cvmanagement.modules.academics.api.error.AcademicLedgerApiException;
import lk.ac.ruhuna.dcs.cvmanagement.modules.academics.application.AcademicLedgerUploadPreflightValidator;
import lk.ac.ruhuna.dcs.cvmanagement.modules.academics.config.AcademicLedgerProperties;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
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
    void validCanonicalExcelWorkbookPassesPreflightWithXlsxContentType() throws Exception {
        byte[] bytes = validXlsx();
        MockMultipartFile file = new MockMultipartFile(
                "file", "ledger.xlsx",
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", bytes);

        var result = validator.validate(file);

        assertThat(result.originalFilename()).isEqualTo("ledger.xlsx");
        assertThat(result.contentType())
                .isEqualTo("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        assertThat(result.checksumSha256()).isEqualTo(HexFormat.of().formatHex(
                MessageDigest.getInstance("SHA-256").digest(bytes)));
    }

    @Test
    void rejectsExcelWorkbookWithInvalidCredits() throws Exception {
        byte[] bytes;
        try (XSSFWorkbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Ledger");
            writeRow(sheet, 0,
                    "student_index_number", "course_code", "credits", "letter_grade",
                    "semester", "academic_year", "attempt_number", "result_status");
            writeRow(sheet, 1, "SC/2025/0001", "CSC1113", "not-a-number", "A", "Semester 1", "2025/2026", "1", "PASSED");
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            workbook.write(out);
            bytes = out.toByteArray();
        }
        MockMultipartFile file = new MockMultipartFile(
                "file", "ledger.xlsx",
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", bytes);

        assertLedgerError(file, 422, "LEDGER_PARSE_FAILED");
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

    private byte[] validXlsx() throws Exception {
        try (XSSFWorkbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Ledger");
            writeRow(sheet, 0,
                    "student_index_number", "course_code", "credits", "letter_grade",
                    "semester", "academic_year", "attempt_number", "result_status");
            Row dataRow = sheet.createRow(1);
            dataRow.createCell(0).setCellValue("SC/2025/0001");
            dataRow.createCell(1).setCellValue("CSC1113");
            dataRow.createCell(2).setCellValue(3.0); // numeric cell, exercises the numeric-cell branch
            dataRow.createCell(3).setCellValue("A");
            dataRow.createCell(4).setCellValue("Semester 1");
            dataRow.createCell(5).setCellValue("2025/2026");
            dataRow.createCell(6).setCellValue(1.0); // numeric cell
            dataRow.createCell(7).setCellValue("PASSED");
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            workbook.write(out);
            return out.toByteArray();
        }
    }

    private void writeRow(Sheet sheet, int rowIndex, String... values) {
        Row row = sheet.createRow(rowIndex);
        for (int index = 0; index < values.length; index++) {
            row.createCell(index).setCellValue(values[index]);
        }
    }

    private String validCsv() {
        return "student_index_number,course_code,credits,letter_grade,semester,academic_year,attempt_number,result_status\r\n"
                + "SC/2025/0001,CSC1113,3.0,A,Semester 1,2025/2026,1,PASSED\r\n";
    }
}
