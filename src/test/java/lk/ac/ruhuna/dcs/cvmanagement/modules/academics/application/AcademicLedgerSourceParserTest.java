package lk.ac.ruhuna.dcs.cvmanagement.modules.academics.application;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import lk.ac.ruhuna.dcs.cvmanagement.modules.academics.api.error.AcademicLedgerErrors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.Test;

class AcademicLedgerSourceParserTest {

    @Test
    void parsesCanonicalRowsInBoundedBatchesAndNormalizesIdentifiers() {
        String csv = """
                student_index_number,course_code,credits,letter_grade,semester,academic_year,attempt_number,result_status
                sc/2025/00001,csc1122,2.0,a-,Semester I,2025/2026,1,passed
                SC/2025/00002,CSC1113,3.0,B+,Semester 2,2025/2026,1,PASSED
                SC/2025/00003,csc113α,1.5,A,Semester 1,2025/2026,1,PASSED
                """;
        AcademicLedgerSourceParser parser = new AcademicLedgerSourceParser(new ObjectMapper());
        var batches = new ArrayList<java.util.List<AcademicLedgerParsedRow>>();

        int count = parser.parse(
                new ByteArrayInputStream(csv.getBytes(StandardCharsets.UTF_8)), "text/csv", 1, batches::add);

        assertThat(count).isEqualTo(3);
        assertThat(batches).hasSize(3);
        AcademicLedgerParsedRow first = batches.get(0).getFirst();
        assertThat(first.rowNumber()).isEqualTo(2);
        assertThat(first.studentIndexNumber()).isEqualTo("SC/2025/00001");
        assertThat(first.courseCode()).isEqualTo("CSC1122");
        assertThat(first.letterGrade()).isEqualTo("A-");
        assertThat(first.semester()).isEqualTo("Semester 1");
        assertThat(first.resultStatus()).isEqualTo("PASSED");
        assertThat(first.rawPayload().get("student_index_number").asText()).isEqualTo("sc/2025/00001");
        assertThat(batches.get(2).getFirst().courseCode()).isEqualTo("CSC113α");
    }

    @Test
    void parsesExcelWorkbookIntoTheSameCanonicalRowShape() throws Exception {
        byte[] xlsx;
        try (XSSFWorkbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Ledger");
            Row header = sheet.createRow(0);
            String[] headers = {
                "student_index_number", "course_code", "credits", "letter_grade",
                "semester", "academic_year", "attempt_number", "result_status"
            };
            for (int i = 0; i < headers.length; i++) {
                header.createCell(i).setCellValue(headers[i]);
            }
            Row data = sheet.createRow(1);
            data.createCell(0).setCellValue("sc/2025/00001");
            data.createCell(1).setCellValue("csc1122");
            data.createCell(2).setCellValue(2.0);
            data.createCell(3).setCellValue("a-");
            data.createCell(4).setCellValue("Semester I");
            data.createCell(5).setCellValue("2025/2026");
            data.createCell(6).setCellValue(1.0);
            data.createCell(7).setCellValue("passed");
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            workbook.write(out);
            xlsx = out.toByteArray();
        }

        AcademicLedgerSourceParser parser = new AcademicLedgerSourceParser(new ObjectMapper());
        var batches = new ArrayList<java.util.List<AcademicLedgerParsedRow>>();

        int count = parser.parse(
                new ByteArrayInputStream(xlsx), AcademicLedgerErrors.XLSX_MEDIA_TYPE, 10, batches::add);

        assertThat(count).isEqualTo(1);
        AcademicLedgerParsedRow row = batches.get(0).getFirst();
        assertThat(row.studentIndexNumber()).isEqualTo("SC/2025/00001");
        assertThat(row.courseCode()).isEqualTo("CSC1122");
        assertThat(row.credits()).isEqualByComparingTo("2.0");
        assertThat(row.letterGrade()).isEqualTo("A-");
        assertThat(row.semester()).isEqualTo("Semester 1");
        assertThat(row.attemptNumber()).isEqualTo((short) 1);
        assertThat(row.resultStatus()).isEqualTo("PASSED");
    }
}
