package lk.ac.ruhuna.dcs.cvmanagement.modules.filtering.persistence.projection;

import java.util.UUID;

/** Immutable filtering-owned projection of the selected internship-request context. */
public record FilterRequestSummaryRow(
        UUID requestId,
        UUID companyId,
        String companyName,
        String title,
        Integer shortlistGuidanceValue) {
}
