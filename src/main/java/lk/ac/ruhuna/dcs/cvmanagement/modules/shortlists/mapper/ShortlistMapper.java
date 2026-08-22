package lk.ac.ruhuna.dcs.cvmanagement.modules.shortlists.mapper;

import lk.ac.ruhuna.dcs.cvmanagement.modules.shortlists.api.dto.response.InternshipRequestSummaryResponse;
import lk.ac.ruhuna.dcs.cvmanagement.modules.shortlists.api.dto.response.ShortlistCandidateResponse;
import lk.ac.ruhuna.dcs.cvmanagement.modules.shortlists.api.dto.response.ShortlistResponse;
import lk.ac.ruhuna.dcs.cvmanagement.modules.shortlists.domain.policy.ShortlistGpaAvailabilityStatus;
import lk.ac.ruhuna.dcs.cvmanagement.modules.shortlists.persistence.projection.ShortlistCandidateRow;
import lk.ac.ruhuna.dcs.cvmanagement.modules.shortlists.persistence.projection.ShortlistSummaryRow;
import org.springframework.stereotype.Component;

/** Maps shortlist-owned persistence projections to public DTOs. */
@Component
public class ShortlistMapper {

    public ShortlistResponse toResponse(ShortlistSummaryRow row) {
        boolean exceeded = row.guidanceValue() != null
                && row.selectedCandidateCount() > row.guidanceValue();
        String warning = exceeded
                ? "Selected candidates exceed the configured shortlist guidance value."
                : null;
        return new ShortlistResponse(
                row.shortlistId(),
                new InternshipRequestSummaryResponse(
                        row.requestId(),
                        row.companyId(),
                        row.companyName(),
                        row.roleTitle(),
                        row.requestGuidanceValue()),
                row.filterRunId(),
                row.name(),
                row.status(),
                row.guidanceValue(),
                row.selectedCandidateCount(),
                exceeded,
                warning,
                row.version(),
                row.createdAt(),
                row.updatedAt(),
                row.finalizedAt());
    }

    public ShortlistCandidateResponse toCandidateResponse(ShortlistCandidateRow row) {
        return new ShortlistCandidateResponse(
                row.studentId(),
                row.indexNumber(),
                row.fullName(),
                row.officialGpa(),
                row.officialGpa() == null
                        ? ShortlistGpaAvailabilityStatus.NOT_AVAILABLE
                        : ShortlistGpaAvailabilityStatus.AVAILABLE,
                row.hasLatestSavedCv(),
                row.activeShortlistCount() > 0,
                row.activeShortlistCount(),
                row.selectedAt(),
                row.selectionNote());
    }
}
