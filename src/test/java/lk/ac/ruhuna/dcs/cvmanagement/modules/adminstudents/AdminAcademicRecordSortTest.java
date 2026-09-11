package lk.ac.ruhuna.dcs.cvmanagement.modules.adminstudents;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import lk.ac.ruhuna.dcs.cvmanagement.modules.adminstudents.domain.exception.AdminStudentApiException;
import lk.ac.ruhuna.dcs.cvmanagement.modules.adminstudents.domain.model.AdminAcademicRecordSort;
import org.junit.jupiter.api.Test;

class AdminAcademicRecordSortTest {

    @Test
    void defaultsToAcademicYearDescendingWithStableRecordTiebreaker() {
        AdminAcademicRecordSort sort = AdminAcademicRecordSort.fromApiValue(null);

        assertThat(sort).isEqualTo(AdminAcademicRecordSort.ACADEMIC_YEAR_DESC);
        assertThat(sort.sqlOrder()).endsWith("g.official_student_grade_id ASC");
    }

    @Test
    void rejectsUnknownSort() {
        assertThatThrownBy(() -> AdminAcademicRecordSort.fromApiValue("gradePoint,asc"))
                .isInstanceOf(AdminStudentApiException.class);
    }
}
