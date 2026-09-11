package lk.ac.ruhuna.dcs.cvmanagement.modules.academics.domain;

import java.util.Arrays;
import lk.ac.ruhuna.dcs.cvmanagement.modules.academics.api.error.AcademicLedgerErrors;

/** Exact OpenAPI v1.6 sort allowlist for committed academic-record inspection. */
public enum AcademicRecordSort {
    ACADEMIC_YEAR_DESC("academicYear,desc", "g.academic_year DESC"),
    COURSE_CODE_ASC("courseCode,asc", "s.course_code ASC"),
    SEMESTER_ASC("semester,asc", "g.semester ASC"),
    GRADE_POINT_DESC("gradePoint,desc", "g.grade_point DESC");

    private final String apiValue;
    private final String sqlOrder;

    AcademicRecordSort(String apiValue, String sqlOrder) {
        this.apiValue = apiValue;
        this.sqlOrder = sqlOrder;
    }

    public String apiValue() { return apiValue; }
    public String sqlOrder() { return sqlOrder; }

    public static AcademicRecordSort fromApiValue(String value) {
        if (value == null) return ACADEMIC_YEAR_DESC;
        return Arrays.stream(values())
                .filter(candidate -> candidate.apiValue.equals(value))
                .findFirst()
                .orElseThrow(() -> AcademicLedgerErrors.badRequest(
                        "sort must be one of academicYear,desc; courseCode,asc; semester,asc; gradePoint,desc."));
    }
}
