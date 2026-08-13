package lk.ac.ruhuna.dcs.cvmanagement.modules.academics.domain;

import java.util.Arrays;
import lk.ac.ruhuna.dcs.cvmanagement.modules.academics.api.error.AcademicLedgerErrors;
import org.springframework.data.domain.Sort;

/** Allowlisted upload-history sort values from OpenAPI v1.6.0. */
public enum AcademicLedgerUploadSort {
    UPLOADED_AT_DESC("uploadedAt,desc", "createdAt", Sort.Direction.DESC),
    UPLOADED_AT_ASC("uploadedAt,asc", "createdAt", Sort.Direction.ASC),
    ORIGINAL_FILENAME_ASC("originalFilename,asc", "fileName", Sort.Direction.ASC),
    STATUS_ASC("status,asc", "uploadStatus", Sort.Direction.ASC),
    STATUS_DESC("status,desc", "uploadStatus", Sort.Direction.DESC);

    public static final String DEFAULT_API_VALUE = "uploadedAt,desc";

    private final String apiValue;
    private final String entityProperty;
    private final Sort.Direction direction;

    AcademicLedgerUploadSort(String apiValue, String entityProperty, Sort.Direction direction) {
        this.apiValue = apiValue;
        this.entityProperty = entityProperty;
        this.direction = direction;
    }

    public String apiValue() {
        return apiValue;
    }

    public Sort sort() {
        return Sort.by(direction, entityProperty).and(Sort.by(Sort.Direction.ASC, "id"));
    }

    public static AcademicLedgerUploadSort fromApiValue(String value) {
        if (value != null && value.isBlank()) {
            throw AcademicLedgerErrors.badRequest("sort must not be blank when supplied.");
        }
        String requested = value == null ? DEFAULT_API_VALUE : value;
        return Arrays.stream(values())
                .filter(candidate -> candidate.apiValue.equals(requested))
                .findFirst()
                .orElseThrow(() -> AcademicLedgerErrors.badRequest("Unsupported Academic Ledger upload sort value."));
    }
}
