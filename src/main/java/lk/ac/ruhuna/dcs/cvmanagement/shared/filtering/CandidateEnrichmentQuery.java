package lk.ac.ruhuna.dcs.cvmanagement.shared.filtering;

import java.util.Map;
import java.util.Set;
import java.util.UUID;

/** Shared factual boundary for BMD-010 CV and BMD-011 shortlist enrichment. */
public interface CandidateEnrichmentQuery {

    Map<UUID, CandidateEnrichment> findAll(Set<UUID> studentIds);

    record CandidateEnrichment(
            boolean hasLatestSavedCv,
            int existingActiveShortlistCount) {

        public CandidateEnrichment {
            if (existingActiveShortlistCount < 0) {
                throw new IllegalArgumentException("existingActiveShortlistCount must not be negative.");
            }
        }

        public boolean hasExistingActiveShortlist() {
            return existingActiveShortlistCount > 0;
        }
    }
}
