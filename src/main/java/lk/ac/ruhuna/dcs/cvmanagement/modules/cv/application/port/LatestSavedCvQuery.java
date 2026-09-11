package lk.ac.ruhuna.dcs.cvmanagement.modules.cv.application.port;

import java.time.OffsetDateTime;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

/** Read-only BMD-007 boundary for Admin inspection and future bulk latest-CV export. */
public interface LatestSavedCvQuery {
    Optional<LatestSavedCv> findByStudentId(UUID studentId);
    Map<UUID, LatestSavedCv> findByStudentIds(Set<UUID> studentIds);

    record LatestSavedCv(
            UUID studentId,
            UUID cvId,
            int revision,
            OffsetDateTime generatedAt,
            OffsetDateTime savedAt,
            String freshnessStatus,
            String fileName,
            long fileSizeBytes) {}
}
