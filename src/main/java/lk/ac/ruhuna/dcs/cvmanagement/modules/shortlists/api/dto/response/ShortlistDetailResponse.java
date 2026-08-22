package lk.ac.ruhuna.dcs.cvmanagement.modules.shortlists.api.dto.response;

import lk.ac.ruhuna.dcs.cvmanagement.shared.pagination.dto.PagedResponse;

/** Shortlist summary and independently paged candidate membership. */
public record ShortlistDetailResponse(
        ShortlistResponse shortlist,
        PagedResponse<ShortlistCandidateResponse> candidates) {
}
