package lk.ac.ruhuna.dcs.cvmanagement.modules.shortlists.api.dto.response;

import java.util.UUID;

/** Shortlists-owned request summary, avoiding cross-module DTO coupling. */
public record InternshipRequestSummaryResponse(
        UUID requestId,
        UUID companyId,
        String companyName,
        String title,
        Integer shortlistGuidanceValue) {
}
