package lk.ac.ruhuna.dcs.cvmanagement.modules.academics.domain;

import java.util.Arrays;
import lk.ac.ruhuna.dcs.cvmanagement.modules.academics.api.error.AcademicLedgerErrors;
import org.springframework.data.domain.Sort;

/** Closed allowlist of v1.6 staged-row sorts. */
public enum AcademicLedgerStagedRowSort {
    ROW_ASC("rowNumber,asc", Sort.Order.asc("rowNumber")),
    ROW_DESC("rowNumber,desc", Sort.Order.desc("rowNumber")),
    STUDENT_ASC("studentIndexNumber,asc", Sort.Order.asc("studentIndexNumber")),
    COURSE_ASC("courseCode,asc", Sort.Order.asc("courseCode")),
    VALIDATION_ASC("validationStatus,asc", Sort.Order.asc("validationStatus"));

    private final String apiValue;
    private final Sort.Order primaryOrder;

    AcademicLedgerStagedRowSort(String apiValue, Sort.Order primaryOrder) {
        this.apiValue = apiValue;
        this.primaryOrder = primaryOrder;
    }

    public String apiValue() {
        return apiValue;
    }

    public Sort sort() {
        return Sort.by(primaryOrder, Sort.Order.asc("id"));
    }

    public static AcademicLedgerStagedRowSort fromApiValue(String value) {
        if (value == null || value.isBlank()) {
            return ROW_ASC;
        }
        return Arrays.stream(values())
                .filter(candidate -> candidate.apiValue.equals(value))
                .findFirst()
                .orElseThrow(() -> AcademicLedgerErrors.badRequest("Unsupported staged-row sort value."));
    }
}
