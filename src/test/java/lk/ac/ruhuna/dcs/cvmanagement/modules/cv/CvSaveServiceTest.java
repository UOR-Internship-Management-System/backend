package lk.ac.ruhuna.dcs.cvmanagement.modules.cv;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Clock;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import lk.ac.ruhuna.dcs.cvmanagement.infrastructure.storage.FileAssetEntity;
import lk.ac.ruhuna.dcs.cvmanagement.infrastructure.storage.FileAssetRepository;
import lk.ac.ruhuna.dcs.cvmanagement.modules.cv.api.dto.response.CvFreshnessResponse;
import lk.ac.ruhuna.dcs.cvmanagement.modules.cv.application.CvActiveSelectionStore;
import lk.ac.ruhuna.dcs.cvmanagement.modules.cv.application.CvFileIntegrityService;
import lk.ac.ruhuna.dcs.cvmanagement.modules.cv.application.CvFreshnessService;
import lk.ac.ruhuna.dcs.cvmanagement.modules.cv.application.CvOrphanFileCleanupService;
import lk.ac.ruhuna.dcs.cvmanagement.modules.cv.application.CvPreviewSelectionStore;
import lk.ac.ruhuna.dcs.cvmanagement.modules.cv.application.CvSaveService;
import lk.ac.ruhuna.dcs.cvmanagement.modules.cv.application.CvSourceFingerprintService;
import lk.ac.ruhuna.dcs.cvmanagement.modules.cv.application.CvSourceQueryService;
import lk.ac.ruhuna.dcs.cvmanagement.modules.cv.domain.exception.CvPreviewExpiredException;
import lk.ac.ruhuna.dcs.cvmanagement.modules.cv.domain.model.CvConfiguration;
import lk.ac.ruhuna.dcs.cvmanagement.modules.cv.domain.model.CvDocumentModel;
import lk.ac.ruhuna.dcs.cvmanagement.modules.cv.domain.policy.CvConditionalRequestPolicy;
import lk.ac.ruhuna.dcs.cvmanagement.modules.cv.persistence.entity.CvEntity;
import lk.ac.ruhuna.dcs.cvmanagement.modules.cv.persistence.entity.CvPreviewEntity;
import lk.ac.ruhuna.dcs.cvmanagement.modules.cv.persistence.entity.CvSourceFreshnessEntity;
import lk.ac.ruhuna.dcs.cvmanagement.modules.cv.persistence.repository.CvPreviewRepository;
import lk.ac.ruhuna.dcs.cvmanagement.modules.cv.persistence.repository.CvRepository;
import lk.ac.ruhuna.dcs.cvmanagement.modules.cv.persistence.repository.CvSourceFreshnessRepository;
import lk.ac.ruhuna.dcs.cvmanagement.modules.studentprofile.persistence.entity.StudentEntity;
import lk.ac.ruhuna.dcs.cvmanagement.modules.studentprofile.persistence.repository.StudentRepository;
import lk.ac.ruhuna.dcs.cvmanagement.shared.audit.AuditEventCategory;
import lk.ac.ruhuna.dcs.cvmanagement.shared.audit.AuditEventPublisher;
import lk.ac.ruhuna.dcs.cvmanagement.shared.audit.AuditEventType;
import lk.ac.ruhuna.dcs.cvmanagement.shared.security.CurrentActor;
import lk.ac.ruhuna.dcs.cvmanagement.shared.security.CurrentActorProvider;
import lk.ac.ruhuna.dcs.cvmanagement.shared.security.RoleName;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class CvSaveServiceTest {

    private static final Instant NOW = Instant.parse("2026-08-19T05:00:00Z");
    private static final String FINGERPRINT = "a".repeat(64);
    private static final String CHECKSUM = "b".repeat(64);

    private final CurrentActorProvider actorProvider = mock(CurrentActorProvider.class);
    private final StudentRepository studentRepository = mock(StudentRepository.class);
    private final CvRepository cvRepository = mock(CvRepository.class);
    private final CvPreviewRepository previewRepository = mock(CvPreviewRepository.class);
    private final CvSourceFreshnessRepository freshnessRepository = mock(CvSourceFreshnessRepository.class);
    private final CvPreviewSelectionStore previewSelectionStore = mock(CvPreviewSelectionStore.class);
    private final CvActiveSelectionStore activeSelectionStore = mock(CvActiveSelectionStore.class);
    private final CvSourceQueryService sourceQueryService = mock(CvSourceQueryService.class);
    private final CvSourceFingerprintService fingerprintService = mock(CvSourceFingerprintService.class);
    private final CvFileIntegrityService fileIntegrityService = mock(CvFileIntegrityService.class);
    private final FileAssetRepository fileAssetRepository = mock(FileAssetRepository.class);
    private final CvFreshnessService freshnessService = mock(CvFreshnessService.class);
    private final CvOrphanFileCleanupService orphanCleanupService = mock(CvOrphanFileCleanupService.class);
    private final AuditEventPublisher auditPublisher = mock(AuditEventPublisher.class);
    private final Clock clock = Clock.fixed(NOW, ZoneOffset.UTC);

    private CvSaveService service;
    private UUID accountId;
    private UUID studentId;
    private StudentEntity student;
    private CvConfiguration configuration;
    private CvDocumentModel document;

    @BeforeEach
    void setUp() {
        accountId = UUID.randomUUID();
        studentId = UUID.randomUUID();
        student = new StudentEntity();
        student.setId(studentId);
        student.setUserAccountId(accountId);
        student.setFullName("CV Student");
        student.setUniversityEmail("cv.student@dcs.ruh.ac.lk");
        student.setUpdatedAt(OffsetDateTime.ofInstant(NOW.minusSeconds(60), ZoneOffset.UTC));
        configuration = new CvConfiguration(List.of(), List.of(), List.of(), List.of(), List.of());
        document = new CvDocumentModel(
                new CvDocumentModel.Identity(studentId, "CV Student", "cv.student@dcs.ruh.ac.lk", student.getUpdatedAt()),
                null, List.of(), List.of(), List.of(), List.of(), List.of(), List.of(), List.of(), null, configuration);

        when(actorProvider.currentActor()).thenReturn(Optional.of(
                new CurrentActor(accountId, "cv.student@dcs.ruh.ac.lk", Set.of(RoleName.STUDENT))));
        when(studentRepository.findByUserAccountIdForUpdate(accountId)).thenReturn(Optional.of(student));
        when(freshnessRepository.findForUpdate(studentId)).thenReturn(Optional.of(new CvSourceFreshnessEntity()));
        when(freshnessService.getFreshness()).thenReturn(new CvFreshnessResponse(
                "CURRENT", List.of(), UUID.randomUUID(), OffsetDateTime.ofInstant(NOW, ZoneOffset.UTC),
                OffsetDateTime.ofInstant(NOW, ZoneOffset.UTC), "Current"));

        service = new CvSaveService(
                actorProvider,
                studentRepository,
                cvRepository,
                previewRepository,
                freshnessRepository,
                previewSelectionStore,
                activeSelectionStore,
                sourceQueryService,
                fingerprintService,
                new CvConditionalRequestPolicy(),
                fileIntegrityService,
                fileAssetRepository,
                freshnessService,
                orphanCleanupService,
                auditPublisher,
                clock);
    }

    @Test
    void firstRealSavePromotesExactPreviewAndCreatesRevisionOne() {
        UUID previewId = UUID.randomUUID();
        UUID assetId = UUID.randomUUID();
        CvPreviewEntity preview = preview(previewId);
        FileAssetEntity persistedAsset = mock(FileAssetEntity.class);
        when(persistedAsset.getId()).thenReturn(assetId);
        when(previewRepository.findOwnedForUpdate(previewId, studentId)).thenReturn(Optional.of(preview));
        when(cvRepository.findByStudentIdForUpdate(studentId)).thenReturn(Optional.empty());
        when(previewSelectionStore.load(previewId)).thenReturn(configuration);
        when(sourceQueryService.load(student, configuration)).thenReturn(document);
        when(fingerprintService.fingerprint(document)).thenReturn(FINGERPRINT);
        when(fileIntegrityService.readVerified(preview.getStagedStorageKey(), 64, CHECKSUM))
                .thenReturn("%PDF-1.7".getBytes(java.nio.charset.StandardCharsets.US_ASCII));
        when(fileAssetRepository.save(any(FileAssetEntity.class))).thenReturn(persistedAsset);
        when(cvRepository.saveAndFlush(any(CvEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var result = service.save(previewId, null, "*");

        assertThat(result.created()).isTrue();
        assertThat(result.response().revision()).isEqualTo(1);
        assertThat(result.response().downloadUrl()).isEqualTo("/me/cv/download");
        assertThat(result.response().pdfFile().fileSizeBytes()).isEqualTo(64);
        assertThat(preview.getConsumedAt()).isEqualTo(OffsetDateTime.ofInstant(NOW, ZoneOffset.UTC));
        assertThat(preview.getResultRevision()).isEqualTo(1);
        verify(activeSelectionStore).replace(eq(result.response().cvId()), eq(studentId), eq(configuration));
        verify(auditPublisher).recordRequired(
                eq(accountId), eq("STUDENT"), eq(AuditEventType.CV_SAVED.name()),
                eq(AuditEventCategory.CV_MANAGEMENT), eq("CV"), anyString(), any());
    }

    @Test
    void sourceFingerprintChangeInvalidatesPreviewBeforeFileAssetCreation() {
        UUID previewId = UUID.randomUUID();
        CvPreviewEntity preview = preview(previewId);
        when(previewRepository.findOwnedForUpdate(previewId, studentId)).thenReturn(Optional.of(preview));
        when(cvRepository.findByStudentIdForUpdate(studentId)).thenReturn(Optional.empty());
        when(previewSelectionStore.load(previewId)).thenReturn(configuration);
        when(sourceQueryService.load(student, configuration)).thenReturn(document);
        when(fingerprintService.fingerprint(document)).thenReturn("c".repeat(64));
        when(fileIntegrityService.readVerified(preview.getStagedStorageKey(), 64, CHECKSUM))
                .thenReturn("%PDF-1.7".getBytes(java.nio.charset.StandardCharsets.US_ASCII));

        assertThatThrownBy(() -> service.save(previewId, null, "*"))
                .isInstanceOf(CvPreviewExpiredException.class);

        verify(fileAssetRepository, never()).save(any());
        verify(cvRepository, never()).saveAndFlush(any());
        verify(auditPublisher, never()).recordRequired(any(), any(), any(), any(), any(), any(), any());
    }

    @Test
    void retryingConsumedPreviewReturnsCurrentResultWithoutRevisionOrFileDuplication() {
        UUID previewId = UUID.randomUUID();
        UUID cvId = UUID.randomUUID();
        CvPreviewEntity preview = preview(previewId);
        preview.setConsumedAt(OffsetDateTime.ofInstant(NOW.minusSeconds(10), ZoneOffset.UTC));
        preview.setResultCvId(cvId);
        preview.setResultRevision(2);
        CvEntity active = activeCv(cvId, 2);
        when(previewRepository.findOwnedForUpdate(previewId, studentId)).thenReturn(Optional.of(preview));
        when(cvRepository.findByStudentIdForUpdate(studentId)).thenReturn(Optional.of(active));
        when(activeSelectionStore.load(cvId)).thenReturn(configuration);

        var result = service.save(previewId, 1L, null);

        assertThat(result.created()).isFalse();
        assertThat(result.response().revision()).isEqualTo(2);
        verify(fileAssetRepository, never()).save(any());
        verify(cvRepository, never()).saveAndFlush(any());
        verify(auditPublisher, never()).recordRequired(any(), any(), any(), any(), any(), any(), any());
    }

    private CvPreviewEntity preview(UUID previewId) {
        OffsetDateTime now = OffsetDateTime.ofInstant(NOW, ZoneOffset.UTC);
        CvPreviewEntity preview = new CvPreviewEntity();
        preview.setPreviewId(previewId);
        preview.setStudentId(studentId);
        preview.setSourceFingerprint(FINGERPRINT);
        preview.setStagedStorageKey("cv/objects/2026/08/" + previewId + ".pdf");
        preview.setStagedFileName("cv-" + studentId + ".pdf");
        preview.setStagedFileSizeBytes(64L);
        preview.setStagedChecksumSha256(CHECKSUM);
        preview.setGeneratedAt(now.minusMinutes(1));
        preview.setExpiresAt(now.plusMinutes(15));
        preview.setCreatedAt(now.minusMinutes(1));
        return preview;
    }

    private CvEntity activeCv(UUID cvId, int revision) {
        OffsetDateTime now = OffsetDateTime.ofInstant(NOW, ZoneOffset.UTC);
        CvEntity cv = new CvEntity();
        cv.setId(cvId);
        cv.setStudentId(studentId);
        cv.setRevision(revision);
        cv.setCreatedAt(now.minusDays(1));
        cv.setGeneratedAt(now.minusMinutes(2));
        cv.setSavedAt(now.minusMinutes(1));
        cv.setUpdatedAt(now.minusMinutes(1));
        cv.setSourceFingerprint(FINGERPRINT);
        cv.setPdfFileAssetId(UUID.randomUUID());
        cv.setPdfFileName("cv-" + studentId + ".pdf");
        cv.setPdfFileSizeBytes(64L);
        return cv;
    }
}
