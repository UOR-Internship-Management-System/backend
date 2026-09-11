package lk.ac.ruhuna.dcs.cvmanagement.modules.shortlists.persistence.projection;

import java.util.UUID;

/** Authoritative Internship Request facts required when creating a shortlist. */
public record ShortlistRequestContext(
        UUID requestId,
        UUID companyId,
        String companyName,
        String title,
        Integer guidanceValue) {
}
