package lk.ac.ruhuna.dcs.cvmanagement.modules.adminstudents;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import lk.ac.ruhuna.dcs.cvmanagement.modules.adminstudents.domain.exception.AdminStudentApiException;
import lk.ac.ruhuna.dcs.cvmanagement.modules.adminstudents.domain.model.RegisteredStudentSort;
import org.junit.jupiter.api.Test;

class RegisteredStudentSortTest {

    @Test
    void defaultsToFullNameAscendingAndIncludesStableStudentIdTiebreaker() {
        RegisteredStudentSort sort = RegisteredStudentSort.fromApiValue(null);

        assertThat(sort).isEqualTo(RegisteredStudentSort.FULL_NAME_ASC);
        assertThat(sort.sqlOrder()).endsWith("student_id ASC");
    }

    @Test
    void allGpaSortsKeepNullsLast() {
        assertThat(RegisteredStudentSort.GPA_ASC.sqlOrder()).contains("NULLS LAST");
        assertThat(RegisteredStudentSort.GPA_DESC.sqlOrder()).contains("NULLS LAST");
    }

    @Test
    void rejectsUnknownSort() {
        assertThatThrownBy(() -> RegisteredStudentSort.fromApiValue("fullName,desc"))
                .isInstanceOf(AdminStudentApiException.class)
                .satisfies(error -> assertThat(((AdminStudentApiException) error).code()).isEqualTo("BAD_REQUEST"));
    }
}
