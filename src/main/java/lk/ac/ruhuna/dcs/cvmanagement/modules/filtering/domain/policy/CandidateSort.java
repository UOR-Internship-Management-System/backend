package lk.ac.ruhuna.dcs.cvmanagement.modules.filtering.domain.policy;

import java.util.Arrays;
import lk.ac.ruhuna.dcs.cvmanagement.shared.error.BadRequestException;

/** Closed allow-list for the public Candidate Filtering sort contract. */
public enum CandidateSort {
    OFFICIAL_GPA_DESC("officialGpa,desc"),
    OFFICIAL_GPA_ASC("officialGpa,asc"),
    FULL_NAME_ASC("fullName,asc"),
    INDEX_NUMBER_ASC("indexNumber,asc");

    public static final CandidateSort DEFAULT = OFFICIAL_GPA_DESC;

    private final String apiValue;

    CandidateSort(String apiValue) {
        this.apiValue = apiValue;
    }

    public String apiValue() {
        return apiValue;
    }

    /**
     * Parses only contract-approved sort values. Arbitrary column/direction input is never accepted.
     * A missing value uses the current Candidate Filtering UI/API default.
     */
    public static CandidateSort fromApiValue(String value) {
        if (value == null || value.isBlank()) {
            return DEFAULT;
        }
        String normalized = value.strip();
        return Arrays.stream(values())
                .filter(candidate -> candidate.apiValue.equals(normalized))
                .findFirst()
                .orElseThrow(() -> new BadRequestException(
                        "sort must be one of officialGpa,desc; officialGpa,asc; fullName,asc; indexNumber,asc."));
    }
}
