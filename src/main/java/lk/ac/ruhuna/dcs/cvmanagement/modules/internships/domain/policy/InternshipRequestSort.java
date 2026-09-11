package lk.ac.ruhuna.dcs.cvmanagement.modules.internships.domain.policy;

import java.util.Locale;
import lk.ac.ruhuna.dcs.cvmanagement.shared.error.ValidationException;

/** Closed allowlist for externally supplied Internship Request sort values. */
public enum InternshipRequestSort {
    CREATED_AT_DESC("createdAt,desc", "ir.created_at DESC, ir.id ASC"),
    TITLE_ASC("title,asc", "LOWER(ir.title) ASC, ir.title ASC, ir.id ASC"),
    COMPANY_NAME_ASC("companyName,asc", "LOWER(c.name) ASC, c.name ASC, ir.id ASC");

    private final String apiValue;
    private final String sqlOrder;

    InternshipRequestSort(String apiValue, String sqlOrder) {
        this.apiValue = apiValue;
        this.sqlOrder = sqlOrder;
    }

    public String apiValue() { return apiValue; }
    public String sqlOrder() { return sqlOrder; }

    public static InternshipRequestSort fromApiValue(String value) {
        if (value == null || value.isBlank()) {
            return CREATED_AT_DESC;
        }
        String normalized = value.strip().toLowerCase(Locale.ROOT);
        for (InternshipRequestSort sort : values()) {
            if (sort.apiValue.toLowerCase(Locale.ROOT).equals(normalized)) {
                return sort;
            }
        }
        throw new ValidationException(
                "sort must be one of createdAt,desc, title,asc, or companyName,asc.");
    }
}
