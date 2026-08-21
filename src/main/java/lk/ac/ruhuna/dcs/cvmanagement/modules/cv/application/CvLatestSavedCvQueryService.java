package lk.ac.ruhuna.dcs.cvmanagement.modules.cv.application;

import java.time.OffsetDateTime;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import lk.ac.ruhuna.dcs.cvmanagement.modules.cv.application.port.LatestSavedCvQuery;
import lk.ac.ruhuna.dcs.cvmanagement.modules.cv.persistence.entity.CvEntity;
import lk.ac.ruhuna.dcs.cvmanagement.modules.cv.persistence.entity.CvSourceFreshnessEntity;
import lk.ac.ruhuna.dcs.cvmanagement.modules.cv.persistence.repository.CvRepository;
import lk.ac.ruhuna.dcs.cvmanagement.modules.cv.persistence.repository.CvSourceFreshnessRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Indexed, read-only latest-CV projection used by Admin and future export consumers. */
@Service
@Transactional(readOnly = true)
public class CvLatestSavedCvQueryService implements LatestSavedCvQuery {
    private static final int MAX_BATCH_SIZE = 500;
    private final CvRepository cvRepository;
    private final CvSourceFreshnessRepository freshnessRepository;

    public CvLatestSavedCvQueryService(CvRepository cvRepository, CvSourceFreshnessRepository freshnessRepository) {
        this.cvRepository = cvRepository;
        this.freshnessRepository = freshnessRepository;
    }

    @Override
    public Optional<LatestSavedCv> findByStudentId(UUID studentId) {
        if (studentId == null) return Optional.empty();
        return cvRepository.findActiveByStudentId(studentId)
                .map(cv -> toRecord(cv, freshnessRepository.findById(studentId).orElse(null)));
    }

    @Override
    public Map<UUID, LatestSavedCv> findByStudentIds(Set<UUID> studentIds) {
        if (studentIds == null || studentIds.isEmpty()) return Map.of();
        if (studentIds.size() > MAX_BATCH_SIZE || studentIds.stream().anyMatch(java.util.Objects::isNull)) {
            throw new IllegalArgumentException("studentIds must contain between 1 and 500 non-null values");
        }
        Collection<CvEntity> cvs = cvRepository.findAllActiveByStudentIdIn(studentIds);
        Map<UUID, CvSourceFreshnessEntity> freshnessByStudent = new HashMap<>();
        freshnessRepository.findAllById(studentIds).forEach(row -> freshnessByStudent.put(row.getStudentId(), row));
        Map<UUID, LatestSavedCv> result = new LinkedHashMap<>();
        cvs.stream().sorted(java.util.Comparator.comparing(cv -> cv.getStudentId().toString()))
                .forEach(cv -> result.put(cv.getStudentId(), toRecord(cv, freshnessByStudent.get(cv.getStudentId()))));
        return Map.copyOf(result);
    }

    private LatestSavedCv toRecord(CvEntity cv, CvSourceFreshnessEntity freshness) {
        return new LatestSavedCv(
                cv.getStudentId(), cv.getId(), cv.getRevision(), cv.getGeneratedAt(), cv.getSavedAt(),
                status(freshness, cv.getSavedAt()), cv.getPdfFileName(), cv.getPdfFileSizeBytes());
    }

    private String status(CvSourceFreshnessEntity row, OffsetDateTime savedAt) {
        if (row == null) return "CURRENT";
        return isAfter(row.getProfileChangedAt(), savedAt)
                || isAfter(row.getDeclaredSkillsChangedAt(), savedAt)
                || isAfter(row.getProjectsChangedAt(), savedAt)
                || isAfter(row.getAcademicRecordsChangedAt(), savedAt)
                ? "OUTDATED" : "CURRENT";
    }

    private boolean isAfter(OffsetDateTime changedAt, OffsetDateTime savedAt) {
        return changedAt != null && changedAt.isAfter(savedAt);
    }
}
