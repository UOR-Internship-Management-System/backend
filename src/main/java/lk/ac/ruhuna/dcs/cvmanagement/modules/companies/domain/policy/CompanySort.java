package lk.ac.ruhuna.dcs.cvmanagement.modules.companies.domain.policy;

import lk.ac.ruhuna.dcs.cvmanagement.shared.error.BadRequestException;

/** Explicit allowlist for the public Company list sort contract. */
public enum CompanySort {
    NAME_ASC("name,asc"),
    NAME_DESC("name,desc"),
    UPDATED_AT_DESC("updatedAt,desc");

    private final String apiValue;

    CompanySort(String apiValue) {
        this.apiValue = apiValue;
    }

    public String apiValue() {
        return apiValue;
    }

    public static CompanySort fromApiValue(String value) {
        if (value == null || value.isBlank()) {
            return NAME_ASC;
        }
        for (CompanySort candidate : values()) {
            if (candidate.apiValue.equals(value)) {
                return candidate;
            }
        }
        throw new BadRequestException(
                "sort must be one of: name,asc; name,desc; updatedAt,desc.");
    }
}
