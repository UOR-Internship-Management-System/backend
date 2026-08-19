package lk.ac.ruhuna.dcs.cvmanagement.modules.cv.application;

import java.time.Clock;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Objects;
import java.util.UUID;
import lk.ac.ruhuna.dcs.cvmanagement.modules.cv.persistence.repository.CvSourceFreshnessRepository;
import lk.ac.ruhuna.dcs.cvmanagement.shared.cv.CvSourceArea;
import lk.ac.ruhuna.dcs.cvmanagement.shared.cv.CvSourceFreshnessUpdatePort;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/** PostgreSQL-backed implementation of the shared CV source-freshness invalidation boundary. */
@Component
public class CvSourceFreshnessUpdater implements CvSourceFreshnessUpdatePort {

    private final CvSourceFreshnessRepository repository;
    private final Clock clock;

    public CvSourceFreshnessUpdater(CvSourceFreshnessRepository repository, Clock clock) {
        this.repository = repository;
        this.clock = clock;
    }

    @Override
    @Transactional(propagation = Propagation.MANDATORY)
    public void markChanged(UUID studentId, CvSourceArea sourceArea) {
        Objects.requireNonNull(studentId, "studentId must not be null");
        Objects.requireNonNull(sourceArea, "sourceArea must not be null");
        OffsetDateTime changedAt = OffsetDateTime.ofInstant(clock.instant(), ZoneOffset.UTC);
        switch (sourceArea) {
            case PROFILE -> repository.upsertProfileChangedAt(studentId, changedAt);
            case DECLARED_SKILLS -> repository.upsertDeclaredSkillsChangedAt(studentId, changedAt);
            case PROJECTS -> repository.upsertProjectsChangedAt(studentId, changedAt);
            case ACADEMIC_RECORDS -> repository.upsertAcademicRecordsChangedAt(studentId, changedAt);
        }
    }
}
