package lk.ac.ruhuna.dcs.cvmanagement.modules.cv;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.time.Clock;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.UUID;
import lk.ac.ruhuna.dcs.cvmanagement.modules.cv.application.CvSourceFreshnessUpdater;
import lk.ac.ruhuna.dcs.cvmanagement.modules.cv.persistence.repository.CvSourceFreshnessRepository;
import lk.ac.ruhuna.dcs.cvmanagement.shared.cv.CvSourceArea;
import org.junit.jupiter.api.Test;

class CvSourceFreshnessUpdaterTest {

    private static final Instant NOW = Instant.parse("2026-08-19T02:00:00Z");

    @Test
    void routesEachTypedSourceAreaToItsAtomicRepositoryUpdate() {
        CvSourceFreshnessRepository repository = mock(CvSourceFreshnessRepository.class);
        CvSourceFreshnessUpdater updater = new CvSourceFreshnessUpdater(repository, Clock.fixed(NOW, ZoneOffset.UTC));
        UUID studentId = UUID.randomUUID();
        OffsetDateTime changedAt = OffsetDateTime.ofInstant(NOW, ZoneOffset.UTC);

        updater.markChanged(studentId, CvSourceArea.PROFILE);
        updater.markChanged(studentId, CvSourceArea.DECLARED_SKILLS);
        updater.markChanged(studentId, CvSourceArea.PROJECTS);
        updater.markChanged(studentId, CvSourceArea.ACADEMIC_RECORDS);

        verify(repository).upsertProfileChangedAt(studentId, changedAt);
        verify(repository).upsertDeclaredSkillsChangedAt(studentId, changedAt);
        verify(repository).upsertProjectsChangedAt(studentId, changedAt);
        verify(repository).upsertAcademicRecordsChangedAt(studentId, changedAt);
    }
}
