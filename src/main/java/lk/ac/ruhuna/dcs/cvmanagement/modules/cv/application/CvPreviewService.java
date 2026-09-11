package lk.ac.ruhuna.dcs.cvmanagement.modules.cv.application;

import java.io.ByteArrayInputStream;
import java.time.Clock;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Map;
import java.util.UUID;
import lk.ac.ruhuna.dcs.cvmanagement.infrastructure.storage.FileStorageException;
import lk.ac.ruhuna.dcs.cvmanagement.infrastructure.storage.FileStoragePort;
import lk.ac.ruhuna.dcs.cvmanagement.modules.cv.api.dto.request.CvPreviewRequest;
import lk.ac.ruhuna.dcs.cvmanagement.modules.cv.api.dto.response.CvPreviewResponse;
import lk.ac.ruhuna.dcs.cvmanagement.modules.cv.domain.exception.CvGenerationFailedException;
import lk.ac.ruhuna.dcs.cvmanagement.modules.cv.domain.model.CvConfiguration;
import lk.ac.ruhuna.dcs.cvmanagement.modules.cv.persistence.entity.CvPreviewEntity;
import lk.ac.ruhuna.dcs.cvmanagement.modules.studentprofile.persistence.entity.StudentEntity;
import lk.ac.ruhuna.dcs.cvmanagement.modules.studentprofile.persistence.repository.StudentRepository;
import lk.ac.ruhuna.dcs.cvmanagement.shared.audit.AuditEventCategory;
import lk.ac.ruhuna.dcs.cvmanagement.shared.audit.AuditEventPublisher;
import lk.ac.ruhuna.dcs.cvmanagement.shared.audit.AuditEventType;
import lk.ac.ruhuna.dcs.cvmanagement.shared.error.ForbiddenException;
import lk.ac.ruhuna.dcs.cvmanagement.shared.error.NotFoundException;
import lk.ac.ruhuna.dcs.cvmanagement.shared.security.CurrentActor;
import lk.ac.ruhuna.dcs.cvmanagement.shared.security.CurrentActorProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/** Creates an owner-scoped preview and stages the exact PDF candidate that a later Save promotes. */
@Service
public class CvPreviewService {

    private final CurrentActorProvider currentActorProvider;
    private final StudentRepository studentRepository;
    private final CvSourceQueryService sourceQueryService;
    private final CvSourceFingerprintService fingerprintService;
    private final CvHtmlRenderer htmlRenderer;
    private final CvGenerationService generationService;
    private final FileStoragePort cvFileStorage;
    private final CvPreviewPersistenceService persistenceService;
    private final CvFreshnessService freshnessService;
    private final AuditEventPublisher auditEventPublisher;
    private final Clock clock;
    private final Duration previewTtl;

    public CvPreviewService(
            CurrentActorProvider currentActorProvider,
            StudentRepository studentRepository,
            CvSourceQueryService sourceQueryService,
            CvSourceFingerprintService fingerprintService,
            CvHtmlRenderer htmlRenderer,
            CvGenerationService generationService,
            @Qualifier("cvFileStorage") FileStoragePort cvFileStorage,
            CvPreviewPersistenceService persistenceService,
            CvFreshnessService freshnessService,
            AuditEventPublisher auditEventPublisher,
            Clock clock,
            @Value("${app.cv.preview-ttl:PT15M}") Duration previewTtl) {
        this.currentActorProvider = currentActorProvider;
        this.studentRepository = studentRepository;
        this.sourceQueryService = sourceQueryService;
        this.fingerprintService = fingerprintService;
        this.htmlRenderer = htmlRenderer;
        this.generationService = generationService;
        this.cvFileStorage = cvFileStorage;
        this.persistenceService = persistenceService;
        this.freshnessService = freshnessService;
        this.auditEventPublisher = auditEventPublisher;
        this.clock = clock;
        this.previewTtl = previewTtl;
    }

    /**
     * External PDF compilation and filesystem I/O intentionally occur outside a database transaction.
     * Only the final preview metadata + selection snapshot is committed atomically.
     */
    public CvPreviewResponse createPreview(CvPreviewRequest request) {
        CurrentActor actor = currentActor();
        StudentEntity student = currentStudent(actor.userId());
        CvConfiguration configuration = CvConfiguration.from(request);
        var document = sourceQueryService.load(student, configuration);
        String sourceFingerprint = fingerprintService.fingerprint(document);
        String htmlPreview = htmlRenderer.render(document);
        byte[] pdf;
        try {
            pdf = generationService.generatePdf(document);
        } catch (CvGenerationFailedException exception) {
            auditGenerationFailure(actor, student);
            throw exception;
        }

        OffsetDateTime generatedAt = OffsetDateTime.ofInstant(clock.instant(), ZoneOffset.UTC);
        OffsetDateTime expiresAt = generatedAt.plus(previewTtl);
        UUID previewId = UUID.randomUUID();
        String fileName = "cv-" + student.getId() + ".pdf";
        String storageKey = storageKey(previewId, generatedAt);

        FileStoragePort.StoredFile stored;
        try {
            stored = cvFileStorage.store(storageKey, new ByteArrayInputStream(pdf));
        } catch (FileStorageException exception) {
            auditGenerationFailure(actor, student);
            throw new CvGenerationFailedException();
        }
        if (stored.sizeBytes() != pdf.length || stored.sizeBytes() < 1) {
            deleteQuietly(storageKey);
            auditGenerationFailure(actor, student);
            throw new CvGenerationFailedException();
        }

        CvPreviewEntity preview = new CvPreviewEntity();
        preview.setPreviewId(previewId);
        preview.setStudentId(student.getId());
        preview.setSourceFingerprint(sourceFingerprint);
        preview.setStagedStorageKey(storageKey);
        preview.setStagedFileName(fileName);
        preview.setStagedFileSizeBytes(stored.sizeBytes());
        preview.setStagedChecksumSha256(stored.checksumSha256());
        preview.setGeneratedAt(generatedAt);
        preview.setExpiresAt(expiresAt);
        preview.setCreatedAt(generatedAt);

        try {
            persistenceService.persist(preview, configuration);
        } catch (RuntimeException exception) {
            deleteQuietly(storageKey);
            throw exception;
        }

        auditEventPublisher.recordBestEffort(
                actor.userId(),
                "STUDENT",
                AuditEventType.CV_PREVIEW_GENERATED.name(),
                AuditEventCategory.CV_MANAGEMENT,
                "CV_PREVIEW",
                previewId.toString(),
                Map.of("studentId", student.getId().toString(), "fileSizeBytes", stored.sizeBytes()));

        return new CvPreviewResponse(
                previewId,
                htmlPreview,
                freshnessService.getFreshness(),
                configuration.toResponse(),
                generatedAt,
                expiresAt);
    }

    private void auditGenerationFailure(CurrentActor actor, StudentEntity student) {
        auditEventPublisher.recordBestEffort(
                actor.userId(),
                "STUDENT",
                AuditEventType.CV_GENERATION_FAILED.name(),
                AuditEventCategory.CV_MANAGEMENT,
                "STUDENT_CV",
                student.getId().toString(),
                Map.of());
    }

    private String storageKey(UUID previewId, OffsetDateTime generatedAt) {
        return "cv/objects/%04d/%02d/%s.pdf".formatted(
                generatedAt.getYear(), generatedAt.getMonthValue(), previewId);
    }

    private void deleteQuietly(String storageKey) {
        try {
            cvFileStorage.delete(storageKey);
        } catch (RuntimeException ignored) {
            // Cleanup is best effort here; the object is opaque and can be reclaimed operationally.
        }
    }

    private CurrentActor currentActor() {
        return currentActorProvider.currentActor()
                .orElseThrow(() -> new ForbiddenException("No authenticated Student context."));
    }

    private StudentEntity currentStudent(UUID accountId) {
        return studentRepository.findByUserAccountId(accountId)
                .orElseThrow(() -> new NotFoundException("Student record not found for the authenticated account."));
    }
}
