package lk.ac.ruhuna.dcs.cvmanagement.modules.adminstudents.domain.model;

import java.util.Arrays;
import lk.ac.ruhuna.dcs.cvmanagement.modules.adminstudents.domain.exception.AdminStudentErrors;

/** Exact OpenAPI v1.6.0 allowlist for registered-Student roster sorting. */
public enum RegisteredStudentSort {
    FULL_NAME_ASC("fullName,asc", "resolved_full_name ASC, student_id ASC"),
    GPA_DESC("officialGpa,desc", "official_gpa DESC NULLS LAST, student_id ASC"),
    GPA_ASC("officialGpa,asc", "official_gpa ASC NULLS LAST, student_id ASC"),
    INDEX_NUMBER_ASC("indexNumber,asc", "index_number ASC, student_id ASC");

    private final String apiValue;
    private final String sqlOrder;

    RegisteredStudentSort(String apiValue, String sqlOrder) {
        this.apiValue = apiValue;
        this.sqlOrder = sqlOrder;
    }

    public String apiValue() {
        return apiValue;
    }

    public String sqlOrder() {
        return sqlOrder;
    }

    public static RegisteredStudentSort fromApiValue(String value) {
        if (value == null || value.isBlank()) {
            return FULL_NAME_ASC;
        }
        return Arrays.stream(values())
                .filter(candidate -> candidate.apiValue.equals(value))
                .findFirst()
                .orElseThrow(() -> AdminStudentErrors.badRequest(
                        "sort must be one of fullName,asc; officialGpa,desc; officialGpa,asc; indexNumber,asc."));
    }
}
