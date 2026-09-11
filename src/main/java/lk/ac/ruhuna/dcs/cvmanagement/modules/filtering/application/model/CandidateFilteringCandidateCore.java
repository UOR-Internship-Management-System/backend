package lk.ac.ruhuna.dcs.cvmanagement.modules.filtering.application.model;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import lk.ac.ruhuna.dcs.cvmanagement.modules.filtering.api.dto.response.CandidateFilteringDeclaredSkillResponse;
import lk.ac.ruhuna.dcs.cvmanagement.modules.filtering.domain.policy.GpaAvailabilityStatus;

/**
 * Candidate data that BMD-010 can resolve from authoritative GPA and declared-skill persistence.
 *
 * <p>CV availability and cross-shortlist facts are deliberately absent. Those values belong to the
 * downstream enrichment gate and must never be fabricated while CV/Shortlist persistence is
 * unavailable.
 */
public record CandidateFilteringCandidateCore(
        UUID studentId,
        String indexNumber,
        String fullName,
        BigDecimal officialGpa,
        GpaAvailabilityStatus gpaAvailabilityStatus,
        List<CandidateFilteringDeclaredSkillResponse> matchingDeclaredSkills,
        int declaredSkillCount) {

    public CandidateFilteringCandidateCore {
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
        if ((officialGpa != null) != (gpaAvailabilityStatus == GpaAvailabilityStatus.AVAILABLE)) {
            throw new IllegalArgumentException("GPA availability must agree with the GPA value.");
        }
    }
}
