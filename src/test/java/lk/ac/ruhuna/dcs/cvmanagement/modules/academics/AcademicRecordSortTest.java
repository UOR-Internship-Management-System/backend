package lk.ac.ruhuna.dcs.cvmanagement.modules.academics;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import lk.ac.ruhuna.dcs.cvmanagement.modules.academics.api.error.AcademicLedgerApiException;
import lk.ac.ruhuna.dcs.cvmanagement.modules.academics.domain.AcademicRecordSort;
import org.junit.jupiter.api.Test;

class AcademicRecordSortTest {
    @Test
    void defaultsToAcademicYearDescending() {
        assertThat(AcademicRecordSort.fromApiValue(null)).isEqualTo(AcademicRecordSort.ACADEMIC_YEAR_DESC);
    }

    @Test
    void acceptsOnlyTheOpenApiV16Allowlist() {
        assertThat(AcademicRecordSort.fromApiValue("courseCode,asc")).isEqualTo(AcademicRecordSort.COURSE_CODE_ASC);
        assertThat(AcademicRecordSort.fromApiValue("semester,asc")).isEqualTo(AcademicRecordSort.SEMESTER_ASC);
        assertThat(AcademicRecordSort.fromApiValue("gradePoint,desc")).isEqualTo(AcademicRecordSort.GRADE_POINT_DESC);
        assertThatThrownBy(() -> AcademicRecordSort.fromApiValue("studentId,asc"))
                .isInstanceOf(AcademicLedgerApiException.class);
    }
}
