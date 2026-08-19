package lk.ac.ruhuna.dcs.cvmanagement.modules.cv.application;

import java.time.Clock;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import lk.ac.ruhuna.dcs.cvmanagement.infrastructure.storage.FileAssetEntity;
import lk.ac.ruhuna.dcs.cvmanagement.infrastructure.storage.FileAssetRepository;
import lk.ac.ruhuna.dcs.cvmanagement.modules.cv.api.dto.response.CvResponse;
import lk.ac.ruhuna.dcs.cvmanagement.modules.cv.api.dto.response.GeneratedFileMetadataResponse;
import lk.ac.ruhuna.dcs.cvmanagement.modules.cv.domain.exception.CvConfigurationInvalidException;
import lk.ac.ruhuna.dcs.cvmanagement.modules.cv.domain.exception.CvNotSavedException;
import lk.ac.ruhuna.dcs.cvmanagement.modules.cv.domain.exception.CvPreviewExpiredException;
import lk.ac.ruhuna.dcs.cvmanagement.modules.cv.domain.model.CvConfiguration;
import lk.ac.ruhuna.dcs.cvmanagement.modules.cv.domain.policy.CvConditionalRequestPolicy;
import lk.ac.ruhuna.dcs.cvmanagement.modules.cv.persistence.entity.CvEntity;
import lk.ac.ruhuna.dcs.cvmanagement.modules.cv.persistence.entity.CvPreviewEntity;
import lk.ac.ruhuna.dcs.cvmanagement.modules.cv.persistence.repository.CvPreviewRepository;
import lk.ac.ruhuna.dcs.cvmanagement.modules.cv.persistence.repository.CvRepository;
import lk.ac.ruhuna.dcs.cvmanagement.modules.cv.persistence.repository.CvSourceFreshnessRepository;
import lk.ac.ruhuna.dcs.cvmanagement.modules.studentprofile.persistence.entity.StudentEntity;
import lk.ac.ruhuna.dcs.cvmanagement.modules.studentprofile.persistence.repository.StudentRepository;
import lk.ac.ruhuna.dcs.cvmanagement.shared.audit.AuditEventCategory;
import lk.ac.ruhuna.dcs.cvmanagement.shared.audit.AuditEventPublisher;
import lk.ac.ruhuna.dcs.cvmanagement.shared.error.ForbiddenException;
import lk.ac.ruhuna.dcs.cvmanagement.shared.error.NotFoundException;
import lk.ac.ruhuna.dcs.cvmanagement.shared.security.CurrentActor;
import lk.ac.ruhuna.dcs.cvmanagement.shared.security.CurrentActorProvider;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

/** Owns the Student's single active saved CV and its optimistic-concurrency revision. */
@Service
public class CvSaveService {

    private static final String PDF_MEDIA_TYPE = "application/pdf";
    private final CurrentActorProvider currentActorProvider;
    private final StudentRepository studentRepository;
    private final CvRepository cvRepository;
    private final CvPreviewRepository previewRepository;
    private final CvSourceFreshnessRepository freshnessRepository;
    private final CvPreviewSelectionStore previewSelectionStore;
    private final CvActiveSelectionStore activeSelectionStore;
    private final CvSourceQueryService sourceQueryService;
    private final CvSourceFingerprintService fingerprintService;
    private final CvConditionalRequestPolicy conditionalRequestPolicy;
    private final CvFileIntegrityService fileIntegrityService;
    private final FileAssetRepository fileAssetRepository;
    private final CvFreshnessService freshnessService;
    private final CvOrphanFileCleanupService orphanFileCleanupService;
    private final AuditEventPublisher auditEventPublisher;
    private final Clock clock;

    public CvSaveService(
            CurrentActorProvider currentActorProvider,
            StudentRepository studentRepository,
            CvRepository cvRepository,
            CvPreviewRepository previewRepository,
            CvSourceFreshnessRepository freshnessRepository,
            CvPreviewSelectionStore previewSelectionStore,
            CvActiveSelectionStore activeSelectionStore,
            CvSourceQueryService sourceQueryService,
            CvSourceFingerprintService fingerprintService,
            CvConditionalRequestPolicy conditionalRequestPolicy,
            CvFileIntegrityService fileIntegrityService,
            FileAssetRepository fileAssetRepository,
            CvFreshnessService freshnessService,
            CvOrphanFileCleanupService orphanFileCleanupService,
            AuditEventPublisher auditEventPublisher,
            Clock clock) {
        this.currentActorProvider = currentActorProvider;
        this.studentRepository = studentRepository;
        this.cvRepository = cvRepository;
        this.previewRepository = previewRepository;
        this.freshnessRepository = freshnessRepository;
        this.previewSelectionStore = previewSelectionStore;
        this.activeSelectionStore = activeSelectionStore;
        this.sourceQueryService = sourceQueryService;
        this.fingerprintService = fingerprintService;
        this.conditionalRequestPolicy = conditionalRequestPolicy;
        this.fileIntegrityService = fileIntegrityService;
        this.fileAssetRepository = fileAssetRepository;
        this.freshnessService = freshnessService;
        this.orphanFileCleanupService = orphanFileCleanupService;
        this.auditEventPublisher = auditEventPublisher;
        this.clock = clock;
    }

    @Transactional(readOnly = true)
    public CvResponse getCurrent() {
        UUID studentId = currentStudentId();
        CvEntity cv = cvRepository.findActiveByStudentId(studentId).orElseThrow(CvNotSavedException::new);
        return toResponse(cv, activeSelectionStore.load(cv.getId()));
    }

    /**
     * Promotes one exact staged preview. PDF compilation never occurs in this transaction.
     * Student, freshness, preview, and active-CV state are serialized before any revision is changed.
     */
    @Transactional
    public CvSaveResult save(UUID previewId, Long ifMatchRevision, String ifNoneMatch) {
        Objects.requireNonNull(previewId, "previewId");
        CurrentActor actor = currentActor();
        StudentEntity student = studentRepository.findByUserAccountIdForUpdate(actor.userId())
                .orElseThrow(() -> new NotFoundException("Student record not found for the authenticated account."));
        UUID studentId = student.getId();
        OffsetDateTime now = OffsetDateTime.ofInstant(clock.instant(), ZoneOffset.UTC);

        freshnessRepository.ensureRow(studentId);
        freshnessRepository.findForUpdate(studentId)
                .orElseThrow(() -> new IllegalStateException("CV source freshness row could not be locked."));

        CvPreviewEntity preview = previewRepository.findOwnedForUpdate(previewId, studentId)
                .orElseThrow(CvPreviewExpiredException::new);
        Optional<CvEntity> persistedRow = cvRepository.findByStudentIdForUpdate(studentId);
        Optional<CvEntity> active = persistedRow.filter(this::isValidActiveCv);

        if (preview.getConsumedAt() != null) {
            if (active.isPresent()
                    && active.get().getId().equals(preview.getResultCvId())
                    && active.get().getRevision() == preview.getResultRevision()) {
                CvResponse response = toResponse(active.get(), activeSelectionStore.load(active.get().getId()));
                return new CvSaveResult(response, preview.getResultRevision() == 1);
            }
            throw new CvPreviewExpiredException();
        }
        if (!preview.getExpiresAt().isAfter(now)) throw new CvPreviewExpiredException();

        conditionalRequestPolicy.validate(
                active.isPresent(), active.map(CvEntity::getRevision).orElse(0), ifMatchRevision, ifNoneMatch);
        validateStagedArtifact(preview);
        try {
            fileIntegrityService.readVerified(
                    preview.getStagedStorageKey(), preview.getStagedFileSizeBytes(), preview.getStagedChecksumSha256());
        } catch (CvFileIntegrityService.FileIntegrityException exception) {
            throw new CvPreviewExpiredException();
        }

        CvConfiguration configuration = previewSelectionStore.load(previewId);
        try {
            var currentDocument = sourceQueryService.load(student, configuration);
            String currentFingerprint = fingerprintService.fingerprint(currentDocument);
            if (!preview.getSourceFingerprint().equals(currentFingerprint)) throw new CvPreviewExpiredException();
        } catch (CvConfigurationInvalidException exception) {
            throw new CvPreviewExpiredException();
        }

        FileAssetEntity newAsset = new FileAssetEntity();
        newAsset.setOwnerAccountId(actor.userId());
        newAsset.setFileName(preview.getStagedFileName());
        newAsset.setStorageKey(preview.getStagedStorageKey());
        newAsset.setMimeType(PDF_MEDIA_TYPE);
        newAsset.setFileSizeBytes(preview.getStagedFileSizeBytes());
        newAsset.setChecksumSha256(preview.getStagedChecksumSha256());
        newAsset = fileAssetRepository.save(newAsset);
        fileAssetRepository.flush();

        UUID oldAssetId = active.map(CvEntity::getPdfFileAssetId).orElse(null);
        CvEntity cv = persistedRow.orElseGet(() -> newCv(studentId, now));
        int nextRevision = active.map(CvEntity::getRevision).orElse(0) + 1;
        if (active.isEmpty() && persistedRow.isPresent()) {
            // Upgrade any pre-Batch-2 zero-byte placeholder as the first real active CV.
            cv.setRevision(0);
        }
        cv.setRevision(nextRevision);
        cv.setGeneratedAt(preview.getGeneratedAt());
        cv.setSavedAt(now);
        cv.setUpdatedAt(now);
        cv.setSourceFingerprint(preview.getSourceFingerprint());
        cv.setLastSavedPreviewId(previewId);
        cv.setPdfFileAssetId(newAsset.getId());
        cv.setPdfFileName(preview.getStagedFileName());
        cv.setPdfFileSizeBytes(preview.getStagedFileSizeBytes());
        clearLegacySelectionColumns(cv);

        CvEntity saved = cvRepository.saveAndFlush(cv);
        activeSelectionStore.replace(saved.getId(), studentId, configuration);

        preview.setConsumedAt(now);
        preview.setResultCvId(saved.getId());
        preview.setResultRevision(saved.getRevision());
        previewRepository.save(preview);

        String eventType = nextRevision == 1 ? "CV_SAVED" : "CV_UPDATED";
        auditEventPublisher.recordRequired(
                actor.userId(),
                "STUDENT",
                eventType,
                AuditEventCategory.CV_MANAGEMENT,
                "CV",
                saved.getId().toString(),
                Map.of("revision", saved.getRevision(), "fileSizeBytes", preview.getStagedFileSizeBytes()));

        registerOldAssetCleanup(oldAssetId, newAsset.getId());
        return new CvSaveResult(toResponse(saved, configuration), nextRevision == 1);
    }

    private CvEntity newCv(UUID studentId, OffsetDateTime now) {
        CvEntity cv = new CvEntity();
        cv.setId(UUID.randomUUID());
        cv.setStudentId(studentId);
        cv.setRevision(0);
        cv.setCreatedAt(now);
        return cv;
    }

    private void validateStagedArtifact(CvPreviewEntity preview) {
        if (preview.getStagedStorageKey() == null
                || preview.getStagedFileName() == null
                || preview.getStagedFileSizeBytes() == null
                || preview.getStagedFileSizeBytes() < 1
                || preview.getStagedChecksumSha256() == null
                || !preview.getStagedChecksumSha256().matches("^[0-9a-f]{64}$")) {
            throw new CvPreviewExpiredException();
        }
    }

    private boolean isValidActiveCv(CvEntity cv) {
        return cv.getPdfFileAssetId() != null
                && cv.getPdfFileSizeBytes() != null
                && cv.getPdfFileSizeBytes() > 0
                && cv.getSourceFingerprint() != null;
    }

    private void clearLegacySelectionColumns(CvEntity cv) {
        cv.setIncludedExperienceIds(null);
        cv.setIncludedProjectIds(null);
        cv.setIncludedCertificateIds(null);
        cv.setIncludedAwardIds(null);
        cv.setIncludedActivityIds(null);
    }

    private void registerOldAssetCleanup(UUID oldAssetId, UUID newAssetId) {
        if (oldAssetId == null || oldAssetId.equals(newAssetId)) return;
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                orphanFileCleanupService.deleteIfUnreferenced(oldAssetId);
            }
        });
    }

    private CvResponse toResponse(CvEntity cv, CvConfiguration configuration) {
        String freshnessStatus = freshnessService.getFreshness().status();
        String effectiveStatus = "NOT_SAVED".equals(freshnessStatus) ? "CURRENT" : freshnessStatus;
        var pdfFile = new GeneratedFileMetadataResponse(
                cv.getPdfFileName(), PDF_MEDIA_TYPE, cv.getPdfFileSizeBytes(), cv.getGeneratedAt());
        return new CvResponse(
                cv.getId(), cv.getRevision(), cv.getCreatedAt(), cv.getGeneratedAt(), cv.getSavedAt(),
                "/me/cv/download", effectiveStatus, configuration.toResponse(), pdfFile);
    }

    private CurrentActor currentActor() {
        return currentActorProvider.currentActor()
                .orElseThrow(() -> new ForbiddenException("No authenticated Student context."));
    }

    private UUID currentStudentId() {
        CurrentActor actor = currentActor();
        return studentRepository.findByUserAccountId(actor.userId())
                .orElseThrow(() -> new NotFoundException("Student record not found for the authenticated account."))
                .getId();
    }
}
