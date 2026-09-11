package lk.ac.ruhuna.dcs.cvmanagement.modules.cv;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.when;

import java.time.Clock;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Optional;
import java.util.UUID;
import lk.ac.ruhuna.dcs.cvmanagement.modules.cv.application.CvSourceFreshnessUpdater;
import lk.ac.ruhuna.dcs.cvmanagement.modules.cv.persistence.entity.CvSourceFreshnessEntity;
import lk.ac.ruhuna.dcs.cvmanagement.modules.cv.persistence.repository.CvSourceFreshnessRepository;
import lk.ac.ruhuna.dcs.cvmanagement.shared.cv.CvSourceArea;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;

class CvSourceFreshnessUpdaterTest {

    private static final Instant NOW = Instant.parse("2026-08-19T02:00:00Z");

    @Test
    void routesEachTypedSourceAreaToItsAtomicRepositoryUpdate() {
        CvSourceFreshnessRepository repository = mock(CvSourceFreshnessRepository.class);
        CvSourceFreshnessUpdater updater = new CvSourceFreshnessUpdater(repository, Clock.fixed(NOW, ZoneOffset.UTC));
        UUID studentId = UUID.randomUUID();
        OffsetDateTime changedAt = OffsetDateTime.ofInstant(NOW, ZoneOffset.UTC);
        when(repository.findForUpdate(studentId))
                .thenReturn(Optional.of(mock(CvSourceFreshnessEntity.class)));

        updater.markChanged(studentId, CvSourceArea.PROFILE);
        updater.markChanged(studentId, CvSourceArea.DECLARED_SKILLS);
        updater.markChanged(studentId, CvSourceArea.PROJECTS);
        updater.markChanged(studentId, CvSourceArea.ACADEMIC_RECORDS);

        verify(repository).upsertProfileChangedAt(studentId, changedAt);
        verify(repository).upsertDeclaredSkillsChangedAt(studentId, changedAt);
        verify(repository).upsertProjectsChangedAt(studentId, changedAt);
        verify(repository).upsertAcademicRecordsChangedAt(studentId, changedAt);
    }
    @Test
    void acquiresFreshnessLockBeforePublishingChangedTimestamp() {
        CvSourceFreshnessRepository repository = mock(CvSourceFreshnessRepository.class);
        UUID studentId = UUID.randomUUID();
        when(repository.findForUpdate(studentId))
                .thenReturn(Optional.of(mock(CvSourceFreshnessEntity.class)));
        CvSourceFreshnessUpdater updater = new CvSourceFreshnessUpdater(repository, Clock.fixed(NOW, ZoneOffset.UTC));

        updater.markChanged(studentId, CvSourceArea.PROFILE);

        InOrder order = inOrder(repository);
        order.verify(repository).ensureRow(studentId);
        order.verify(repository).findForUpdate(studentId);
        order.verify(repository).upsertProfileChangedAt(studentId, OffsetDateTime.ofInstant(NOW, ZoneOffset.UTC));
    }

}
