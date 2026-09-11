package lk.ac.ruhuna.dcs.cvmanagement.modules.adminstudents.domain.model;

import java.util.Arrays;
import lk.ac.ruhuna.dcs.cvmanagement.modules.adminstudents.domain.exception.AdminStudentErrors;

/** Exact OpenAPI v1.6.0 allowlist for one Student's official academic-record sorting. */
public enum AdminAcademicRecordSort {
    ACADEMIC_YEAR_DESC("academicYear,desc", "g.academic_year DESC, g.official_student_grade_id ASC"),
    COURSE_CODE_ASC("courseCode,asc", "s.course_code ASC, g.official_student_grade_id ASC"),
    SEMESTER_ASC("semester,asc", "g.semester ASC, g.official_student_grade_id ASC"),
    GRADE_POINT_DESC("gradePoint,desc", "g.grade_point DESC NULLS LAST, g.official_student_grade_id ASC");

    private final String apiValue;
    private final String sqlOrder;

    AdminAcademicRecordSort(String apiValue, String sqlOrder) {
        this.apiValue = apiValue;
        this.sqlOrder = sqlOrder;
    }

    public String apiValue() {
        return apiValue;
    }

    public String sqlOrder() {
        return sqlOrder;
    }

    public static AdminAcademicRecordSort fromApiValue(String value) {
        if (value == null || value.isBlank()) {
            return ACADEMIC_YEAR_DESC;
        }
        return Arrays.stream(values())
                .filter(candidate -> candidate.apiValue.equals(value))
                .findFirst()
                .orElseThrow(() -> AdminStudentErrors.badRequest(
                        "sort must be one of academicYear,desc; courseCode,asc; semester,asc; gradePoint,desc."));
    }
}
