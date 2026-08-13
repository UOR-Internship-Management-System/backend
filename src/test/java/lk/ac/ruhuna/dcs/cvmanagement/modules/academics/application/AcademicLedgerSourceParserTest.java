package lk.ac.ruhuna.dcs.cvmanagement.modules.academics.application;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import org.junit.jupiter.api.Test;

class AcademicLedgerSourceParserTest {

    @Test
    void parsesCanonicalRowsInBoundedBatchesAndNormalizesIdentifiers() {
        String csv = """
                student_index_number,course_code,credits,letter_grade,semester,academic_year,attempt_number,result_status
                sc/2025/00001,csc1122,2.0,a-,Semester I,2025/2026,1,passed
                SC/2025/00002,CSC1113,3.0,B+,Semester 2,2025/2026,1,PASSED
                """;
        AcademicLedgerSourceParser parser = new AcademicLedgerSourceParser(new ObjectMapper());
        var batches = new ArrayList<java.util.List<AcademicLedgerParsedRow>>();

        int count = parser.parse(
                new ByteArrayInputStream(csv.getBytes(StandardCharsets.UTF_8)), 1, batches::add);

        assertThat(count).isEqualTo(2);
        assertThat(batches).hasSize(2);
        AcademicLedgerParsedRow first = batches.get(0).getFirst();
        assertThat(first.rowNumber()).isEqualTo(2);
        assertThat(first.studentIndexNumber()).isEqualTo("SC/2025/00001");
        assertThat(first.courseCode()).isEqualTo("CSC1122");
        assertThat(first.letterGrade()).isEqualTo("A-");
        assertThat(first.semester()).isEqualTo("Semester 1");
        assertThat(first.resultStatus()).isEqualTo("PASSED");
        assertThat(first.rawPayload().get("student_index_number").asText()).isEqualTo("sc/2025/00001");
    }
}
