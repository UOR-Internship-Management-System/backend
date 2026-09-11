package lk.ac.ruhuna.dcs.cvmanagement.modules.cv;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import lk.ac.ruhuna.dcs.cvmanagement.modules.cv.application.CvLatestSavedCvQueryService;
import lk.ac.ruhuna.dcs.cvmanagement.modules.cv.persistence.entity.CvEntity;
import lk.ac.ruhuna.dcs.cvmanagement.modules.cv.persistence.entity.CvSourceFreshnessEntity;
import lk.ac.ruhuna.dcs.cvmanagement.modules.cv.persistence.repository.CvRepository;
import lk.ac.ruhuna.dcs.cvmanagement.modules.cv.persistence.repository.CvSourceFreshnessRepository;
import org.junit.jupiter.api.Test;

class CvLatestSavedCvQueryServiceTest {

    @Test
    void batchResolutionUsesBoundedSetQueriesAndReportsOutdatedState() {
        UUID first = UUID.fromString("10000000-0000-4000-8000-000000000001");
        UUID second = UUID.fromString("10000000-0000-4000-8000-000000000002");
        OffsetDateTime savedAt = OffsetDateTime.parse("2026-08-19T05:00:00Z");
        CvRepository cvs = mock(CvRepository.class);
        CvSourceFreshnessRepository freshness = mock(CvSourceFreshnessRepository.class);
        when(cvs.findAllActiveByStudentIdIn(Set.of(first, second)))
                .thenReturn(List.of(cv(first, savedAt), cv(second, savedAt)));
        CvSourceFreshnessEntity firstFreshness = freshness(first, savedAt.plusSeconds(1));
        CvSourceFreshnessEntity secondFreshness = freshness(second, savedAt.minusSeconds(1));
        when(freshness.findAllById(Set.of(first, second))).thenReturn(List.of(firstFreshness, secondFreshness));

        var service = new CvLatestSavedCvQueryService(cvs, freshness);
        var result = service.findByStudentIds(Set.of(first, second));

        assertThat(result).hasSize(2);
        assertThat(result.get(first).freshnessStatus()).isEqualTo("OUTDATED");
        assertThat(result.get(second).freshnessStatus()).isEqualTo("CURRENT");
        verify(cvs).findAllActiveByStudentIdIn(Set.of(first, second));
        verify(freshness).findAllById(Set.of(first, second));
    }

    private CvEntity cv(UUID studentId, OffsetDateTime savedAt) {
        CvEntity cv = new CvEntity();
        cv.setId(UUID.randomUUID());
        cv.setStudentId(studentId);
        cv.setRevision(2);
        cv.setGeneratedAt(savedAt.minusSeconds(10));
        cv.setSavedAt(savedAt);
        cv.setPdfFileName("cv-" + studentId + ".pdf");
        cv.setPdfFileSizeBytes(1024L);
        return cv;
    }

    private CvSourceFreshnessEntity freshness(UUID studentId, OffsetDateTime profileChangedAt) {
        CvSourceFreshnessEntity row = new CvSourceFreshnessEntity();
        row.setStudentId(studentId);
        row.setProfileChangedAt(profileChangedAt);
        return row;
    }
}
