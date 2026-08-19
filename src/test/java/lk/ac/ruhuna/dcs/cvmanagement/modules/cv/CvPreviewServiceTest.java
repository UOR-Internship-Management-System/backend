package lk.ac.ruhuna.dcs.cvmanagement.modules.cv;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import lk.ac.ruhuna.dcs.cvmanagement.modules.cv.api.dto.request.CvPreviewRequest;
import lk.ac.ruhuna.dcs.cvmanagement.modules.cv.api.dto.response.CvFreshnessResponse;
import lk.ac.ruhuna.dcs.cvmanagement.modules.cv.application.CvFreshnessService;
import lk.ac.ruhuna.dcs.cvmanagement.modules.cv.application.CvHtmlRenderer;
import lk.ac.ruhuna.dcs.cvmanagement.modules.cv.application.CvPreviewSelectionStore;
import lk.ac.ruhuna.dcs.cvmanagement.modules.cv.application.CvPreviewService;
import lk.ac.ruhuna.dcs.cvmanagement.modules.cv.application.CvSourceFingerprintService;
import lk.ac.ruhuna.dcs.cvmanagement.modules.cv.application.CvSourceQueryService;
import lk.ac.ruhuna.dcs.cvmanagement.modules.cv.domain.model.CvConfiguration;
import lk.ac.ruhuna.dcs.cvmanagement.modules.cv.domain.model.CvDocumentModel;
import lk.ac.ruhuna.dcs.cvmanagement.modules.cv.persistence.entity.CvPreviewEntity;
import lk.ac.ruhuna.dcs.cvmanagement.modules.cv.persistence.repository.CvPreviewRepository;
import lk.ac.ruhuna.dcs.cvmanagement.modules.studentprofile.persistence.entity.StudentEntity;
import lk.ac.ruhuna.dcs.cvmanagement.modules.studentprofile.persistence.repository.StudentRepository;
import lk.ac.ruhuna.dcs.cvmanagement.shared.security.CurrentActor;
import lk.ac.ruhuna.dcs.cvmanagement.shared.security.CurrentActorProvider;
import lk.ac.ruhuna.dcs.cvmanagement.shared.security.RoleName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

class CvPreviewServiceTest {

    @Test
    void persistsDurablePreviewFingerprintAndCanonicalSelectionSnapshot() {
        Instant instant = Instant.parse("2026-08-19T02:00:00Z");
        Clock clock = Clock.fixed(instant, ZoneOffset.UTC);
        UUID accountId = UUID.randomUUID();
        UUID studentId = UUID.randomUUID();
        UUID experienceId = UUID.randomUUID();

        CurrentActorProvider actors = mock(CurrentActorProvider.class);
        StudentRepository students = mock(StudentRepository.class);
        CvSourceQueryService sourceQuery = mock(CvSourceQueryService.class);
        CvSourceFingerprintService fingerprints = mock(CvSourceFingerprintService.class);
        CvHtmlRenderer htmlRenderer = mock(CvHtmlRenderer.class);
        CvPreviewRepository previews = mock(CvPreviewRepository.class);
        CvPreviewSelectionStore selections = mock(CvPreviewSelectionStore.class);
        CvFreshnessService freshness = mock(CvFreshnessService.class);

        StudentEntity student = new StudentEntity();
        student.setId(studentId);
        student.setFullName("Student");
        student.setUniversityEmail("student@ruh.ac.lk");
        student.setUpdatedAt(OffsetDateTime.ofInstant(instant, ZoneOffset.UTC));
        when(actors.currentActor()).thenReturn(Optional.of(
                new CurrentActor(accountId, "student@ruh.ac.lk", Set.of(RoleName.STUDENT))));
        when(students.findByUserAccountId(accountId)).thenReturn(Optional.of(student));

        CvDocumentModel document = new CvDocumentModel(
                new CvDocumentModel.Identity(studentId, "Student", "student@ruh.ac.lk", student.getUpdatedAt()),
                null, List.of(), List.of(), List.of(), List.of(), List.of(), List.of(), List.of(), null,
                new CvConfiguration(List.of(experienceId), List.of(), List.of(), List.of(), List.of()));
        when(sourceQuery.load(any(), any())).thenReturn(document);
        when(fingerprints.fingerprint(document)).thenReturn("a".repeat(64));
        when(htmlRenderer.render(document)).thenReturn("<div>preview</div>");
        when(freshness.getFreshness()).thenReturn(new CvFreshnessResponse(
                "NOT_SAVED", List.of(), null, null, OffsetDateTime.ofInstant(instant, ZoneOffset.UTC), "Not saved"));

        CvPreviewService service = new CvPreviewService(
                actors, students, sourceQuery, fingerprints, htmlRenderer, previews, selections, freshness,
                clock, Duration.ofMinutes(15));
        CvPreviewRequest request = new CvPreviewRequest(
                List.of(experienceId), List.of(), List.of(), List.of(), List.of());

        var response = service.createPreview(request);

        ArgumentCaptor<CvPreviewEntity> captor = ArgumentCaptor.forClass(CvPreviewEntity.class);
        verify(previews).save(captor.capture());
        CvPreviewEntity persisted = captor.getValue();
        assertThat(persisted.getStudentId()).isEqualTo(studentId);
        assertThat(persisted.getSourceFingerprint()).isEqualTo("a".repeat(64));
        assertThat(persisted.getGeneratedAt()).isEqualTo(OffsetDateTime.ofInstant(instant, ZoneOffset.UTC));
        assertThat(persisted.getExpiresAt()).isEqualTo(persisted.getGeneratedAt().plusMinutes(15));
        verify(selections).save(persisted.getPreviewId(), studentId, document.configuration());
        assertThat(response.previewId()).isEqualTo(persisted.getPreviewId());
        assertThat(response.configuration().includedExperienceIds()).containsExactly(experienceId);
    }
}
