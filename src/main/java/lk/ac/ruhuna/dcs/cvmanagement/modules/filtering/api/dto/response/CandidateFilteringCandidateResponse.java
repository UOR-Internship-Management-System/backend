package lk.ac.ruhuna.dcs.cvmanagement.modules.filtering.api.dto.response;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import lk.ac.ruhuna.dcs.cvmanagement.modules.filtering.domain.policy.GpaAvailabilityStatus;

/** Public row contract for one deterministic Candidate Filtering result. */
public record CandidateFilteringCandidateResponse(
        UUID studentId,
        String indexNumber,
        String fullName,
        BigDecimal officialGpa,
        GpaAvailabilityStatus gpaAvailabilityStatus,
        List<CandidateFilteringDeclaredSkillResponse> matchingDeclaredSkills,
        int declaredSkillCount,
        boolean hasLatestSavedCv,
        boolean hasExistingActiveShortlist,
        int existingActiveShortlistCount) {

    private static final BigDecimal MIN_GPA = new BigDecimal("0.00");
    private static final BigDecimal MAX_GPA = new BigDecimal("4.00");

    public CandidateFilteringCandidateResponse {
        Objects.requireNonNull(studentId, "studentId is required.");
        Objects.requireNonNull(indexNumber, "indexNumber is required.");
        Objects.requireNonNull(fullName, "fullName is required.");
        Objects.requireNonNull(gpaAvailabilityStatus, "gpaAvailabilityStatus is required.");
        matchingDeclaredSkills = matchingDeclaredSkills == null
                ? List.of()
                : List.copyOf(matchingDeclaredSkills);

        if (declaredSkillCount < 0) {
            throw new IllegalArgumentException("declaredSkillCount must not be negative.");
        }
        if (existingActiveShortlistCount < 0) {
            throw new IllegalArgumentException("existingActiveShortlistCount must not be negative.");
        }
        if (officialGpa != null
                && (officialGpa.compareTo(MIN_GPA) < 0 || officialGpa.compareTo(MAX_GPA) > 0)) {
            throw new IllegalArgumentException("officialGpa must be between 0.00 and 4.00.");
        }
        if ((officialGpa != null) != (gpaAvailabilityStatus == GpaAvailabilityStatus.AVAILABLE)) {
            throw new IllegalArgumentException("GPA availability must agree with the GPA value.");
        }
        if (hasExistingActiveShortlist != (existingActiveShortlistCount > 0)) {
            throw new IllegalArgumentException("Shortlist availability must agree with its count.");
        }
    }
}
