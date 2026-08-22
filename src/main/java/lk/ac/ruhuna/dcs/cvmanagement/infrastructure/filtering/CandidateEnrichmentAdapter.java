package lk.ac.ruhuna.dcs.cvmanagement.infrastructure.filtering;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import lk.ac.ruhuna.dcs.cvmanagement.modules.cv.application.port.LatestSavedCvQuery;
import lk.ac.ruhuna.dcs.cvmanagement.modules.shortlists.persistence.query.ShortlistReadRepository;
import lk.ac.ruhuna.dcs.cvmanagement.shared.filtering.CandidateEnrichmentQuery;
import org.springframework.stereotype.Component;

/** Infrastructure composition of authoritative BMD-007 and BMD-011 candidate facts. */
@Component
public class CandidateEnrichmentAdapter implements CandidateEnrichmentQuery {

    private final LatestSavedCvQuery latestSavedCvQuery;
    private final ShortlistReadRepository shortlistReadRepository;

    public CandidateEnrichmentAdapter(
            LatestSavedCvQuery latestSavedCvQuery,
            ShortlistReadRepository shortlistReadRepository) {
        this.latestSavedCvQuery = latestSavedCvQuery;
        this.shortlistReadRepository = shortlistReadRepository;
    }

    @Override
    public Map<UUID, CandidateEnrichment> findAll(Set<UUID> studentIds) {
        if (studentIds == null || studentIds.isEmpty()) {
            return Map.of();
        }
        Map<UUID, LatestSavedCvQuery.LatestSavedCv> cvs = latestSavedCvQuery.findByStudentIds(studentIds);
        Map<UUID, Integer> shortlistCounts = shortlistReadRepository.countActiveShortlists(studentIds);
        Map<UUID, CandidateEnrichment> result = new LinkedHashMap<>();
        studentIds.stream()
                .sorted(java.util.Comparator.comparing(UUID::toString))
                .forEach(studentId -> result.put(
                        studentId,
                        new CandidateEnrichment(
                                cvs.containsKey(studentId),
                                shortlistCounts.getOrDefault(studentId, 0))));
        return Map.copyOf(result);
    }
}
