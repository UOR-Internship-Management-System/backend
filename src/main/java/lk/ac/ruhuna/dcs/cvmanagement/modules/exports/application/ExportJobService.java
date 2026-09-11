package lk.ac.ruhuna.dcs.cvmanagement.modules.exports.application;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.time.Clock;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.EnumSet;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import lk.ac.ruhuna.dcs.cvmanagement.infrastructure.storage.FileAssetEntity;
import lk.ac.ruhuna.dcs.cvmanagement.infrastructure.storage.FileAssetRepository;
import lk.ac.ruhuna.dcs.cvmanagement.infrastructure.storage.FileStoragePort;
import lk.ac.ruhuna.dcs.cvmanagement.modules.exports.api.dto.response.ExportFileResponse;
import lk.ac.ruhuna.dcs.cvmanagement.modules.exports.api.dto.response.ExportJobResponse;
import lk.ac.ruhuna.dcs.cvmanagement.modules.exports.config.ExportProperties;
import lk.ac.ruhuna.dcs.cvmanagement.modules.exports.domain.policy.ExportFormat;
import lk.ac.ruhuna.dcs.cvmanagement.modules.exports.domain.policy.ExportStatus;
import lk.ac.ruhuna.dcs.cvmanagement.modules.exports.domain.policy.ExportType;
import lk.ac.ruhuna.dcs.cvmanagement.modules.exports.domain.policy.ExportWarningCode;
import lk.ac.ruhuna.dcs.cvmanagement.modules.exports.mapper.ExportMapper;
import lk.ac.ruhuna.dcs.cvmanagement.modules.exports.persistence.entity.ExportFileEntity;
import lk.ac.ruhuna.dcs.cvmanagement.modules.exports.persistence.entity.ExportJobEntity;
import lk.ac.ruhuna.dcs.cvmanagement.modules.exports.persistence.entity.ExportWarningEntity;
import lk.ac.ruhuna.dcs.cvmanagement.modules.exports.persistence.query.ExportReadRepository;
import lk.ac.ruhuna.dcs.cvmanagement.modules.exports.persistence.repository.ExportFileRepository;
import lk.ac.ruhuna.dcs.cvmanagement.modules.exports.persistence.repository.ExportJobRepository;
import lk.ac.ruhuna.dcs.cvmanagement.modules.exports.persistence.repository.ExportWarningRepository;
import lk.ac.ruhuna.dcs.cvmanagement.shared.audit.AuditEventCategory;
import lk.ac.ruhuna.dcs.cvmanagement.shared.audit.AuditEventPublisher;
import lk.ac.ruhuna.dcs.cvmanagement.shared.audit.AuditEventType;
import lk.ac.ruhuna.dcs.cvmanagement.shared.error.ConflictException;
import lk.ac.ruhuna.dcs.cvmanagement.shared.error.DependencyUnavailableException;
import lk.ac.ruhuna.dcs.cvmanagement.shared.error.ForbiddenException;
import lk.ac.ruhuna.dcs.cvmanagement.shared.error.NotFoundException;
import lk.ac.ruhuna.dcs.cvmanagement.shared.error.UnauthorizedException;
import lk.ac.ruhuna.dcs.cvmanagement.shared.error.ValidationException;
import lk.ac.ruhuna.dcs.cvmanagement.shared.security.CurrentActor;
import lk.ac.ruhuna.dcs.cvmanagement.shared.security.CurrentActorProvider;
import lk.ac.ruhuna.dcs.cvmanagement.shared.security.RoleName;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Transactional lifecycle for asynchronous shortlist export jobs. */
@Service
public class ExportJobService {
    private static final String AUDIT_RESOURCE = "EXPORT_JOB";
    private static final Duration PROCESSING_TIMEOUT = Duration.ofMinutes(10);
    private static final EnumSet<ExportStatus> ACTIVE_STATUSES =
            EnumSet.of(ExportStatus.QUEUED, ExportStatus.PROCESSING);

    private final ExportJobRepository jobRepository;
    private final ExportFileRepository missingRepository;
    private final ExportWarningRepository warningRepository;
    private final ExportReadRepository readRepository;
    private final FileAssetRepository fileAssetRepository;
    private final FileStoragePort fileStorage;
    private final ExportMapper mapper;
    private final CurrentActorProvider currentActorProvider;
    private final AuditEventPublisher auditEventPublisher;
    private final ExportProperties properties;
    private final Clock clock;

    public ExportJobService(
            ExportJobRepository jobRepository,
            ExportFileRepository missingRepository,
            ExportWarningRepository warningRepository,
            ExportReadRepository readRepository,
            FileAssetRepository fileAssetRepository,
            @Qualifier("exportFileStorage") FileStoragePort fileStorage,
            ExportMapper mapper,
            CurrentActorProvider currentActorProvider,
            AuditEventPublisher auditEventPublisher,
            ExportProperties properties,
            Clock clock) {
        this.jobRepository = jobRepository;
        this.missingRepository = missingRepository;
        this.warningRepository = warningRepository;
        this.readRepository = readRepository;
        this.fileAssetRepository = fileAssetRepository;
        this.fileStorage = fileStorage;
        this.mapper = mapper;
        this.currentActorProvider = currentActorProvider;
        this.auditEventPublisher = auditEventPublisher;
        this.properties = properties;
        this.clock = clock;
    }

    @Transactional
    public ExportJobResponse create(UUID shortlistId, ExportType type, ExportFormat format) {
        CurrentActor actor = currentAdmin();
        validateFormat(type, format);
        var shortlist = readRepository.findShortlist(shortlistId)
                .orElseThrow(() -> new NotFoundException("Shortlist was not found."));
        if (!shortlist.finalized()) {
            throw new ConflictException("Only finalized shortlists can be exported.");
        }
        if (jobRepository.existsByShortlistIdAndExportTypeAndStatusIn(shortlistId, type, ACTIVE_STATUSES)) {
            throw new ConflictException("An active export of this type already exists for the shortlist.");
        }
        ExportJobEntity job = new ExportJobEntity();
        job.setId(UUID.randomUUID());
        job.setShortlistId(shortlistId);
        job.setExportType(type);
        job.setFormat(format);
        job.setStatus(ExportStatus.QUEUED);
        job.setRequestedByAccountId(actor.userId());
        job.setCreatedAt(OffsetDateTime.now(clock));
        try {
            jobRepository.saveAndFlush(job);
        } catch (DataIntegrityViolationException exception) {
            throw new ConflictException("An active export of this type already exists for the shortlist.");
        }
        auditEventPublisher.recordRequired(
                actor.userId(), RoleName.ADMIN.name(), AuditEventType.EXPORT_JOB_CREATED.name(),
                AuditEventCategory.EXPORT_MANAGEMENT, AUDIT_RESOURCE, job.getId().toString(),
                Map.of("shortlistId", shortlistId, "exportType", type.name()));
        return response(job);
    }

    @Transactional(readOnly = true)
    public ExportJobResponse get(UUID exportJobId) {
        currentAdmin();
        return response(requireJob(exportJobId));
    }

    @Transactional
    public ExportFileResponse download(UUID exportJobId, ExportType expectedType) {
        CurrentActor actor = currentAdmin();
        ExportJobEntity job = requireJob(exportJobId);
        if (job.getExportType() != expectedType) {
            throw new NotFoundException("Export job was not found.");
        }
        OffsetDateTime now = OffsetDateTime.now(clock);
        if (job.getStatus() != ExportStatus.COMPLETED || job.getFileAssetId() == null) {
            throw new ConflictException("The export is not ready for download.");
        }
        if (job.getExpiresAt() != null && !job.getExpiresAt().isAfter(now)) {
            throw new ConflictException("The export download has expired.");
        }
        FileAssetEntity asset = fileAssetRepository.findById(job.getFileAssetId())
                .orElseThrow(() -> new DependencyUnavailableException("The export file is unavailable."));
        InputStream content;
        try {
            content = fileStorage.open(asset.getStorageKey());
        } catch (RuntimeException exception) {
            throw new DependencyUnavailableException("The export file is unavailable.");
        }
        auditEventPublisher.recordRequired(
                actor.userId(), RoleName.ADMIN.name(), AuditEventType.EXPORT_DOWNLOADED.name(),
                AuditEventCategory.EXPORT_MANAGEMENT, AUDIT_RESOURCE, job.getId().toString(), Map.of());
        return new ExportFileResponse(asset.getFileName(), asset.getMimeType(), asset.getFileSizeBytes(), content);
    }

    @Transactional
    public Optional<ExportJobSnapshot> claimNext() {
        failInterruptedJob();
        Optional<ExportJobEntity> queued = jobRepository.findFirstByStatusOrderByCreatedAtAscIdAsc(ExportStatus.QUEUED);
        if (queued.isEmpty()) {
            return Optional.empty();
        }
        ExportJobEntity job = queued.get();
        job.setStatus(ExportStatus.PROCESSING);
        job.setStartedAt(OffsetDateTime.now(clock));
        jobRepository.saveAndFlush(job);
        return Optional.of(new ExportJobSnapshot(job.getId(), job.getShortlistId(), job.getExportType()));
    }

    private void failInterruptedJob() {
        jobRepository.findFirstByStatusAndStartedAtBeforeOrderByStartedAtAscIdAsc(
                        ExportStatus.PROCESSING, OffsetDateTime.now(clock).minus(PROCESSING_TIMEOUT))
                .ifPresent(job -> {
                    job.setStatus(ExportStatus.FAILED);
                    job.setFailureCode("WORKER_INTERRUPTED");
                    job.setFailureMessage("Export processing was interrupted and can be requested again.");
                    job.setCompletedAt(OffsetDateTime.now(clock));
                    jobRepository.saveAndFlush(job);
                    auditEventPublisher.recordRequired(
                            job.getRequestedByAccountId(), RoleName.ADMIN.name(),
                            AuditEventType.EXPORT_JOB_FAILED.name(), AuditEventCategory.EXPORT_MANAGEMENT,
                            AUDIT_RESOURCE, job.getId().toString(), Map.of("failureCode", "WORKER_INTERRUPTED"));
                });
    }

    @Transactional
    public void complete(UUID exportJobId, GeneratedExport generated, String storageKey) {
        ExportJobEntity job = requireJob(exportJobId);
        if (job.getStatus() != ExportStatus.PROCESSING) {
            throw new ConflictException("Only a processing export can be completed.");
        }
        FileStoragePort.StoredFile stored;
        try (InputStream content = Files.newInputStream(generated.path())) {
            stored = fileStorage.store(storageKey, content);
        } catch (IOException | RuntimeException exception) {
            throw new ExportGenerationException("EXPORT_STORAGE_FAILED", "Unable to store the generated export.", exception);
        }
        try {
            FileAssetEntity asset = new FileAssetEntity();
            asset.setOwnerAccountId(job.getRequestedByAccountId());
            asset.setFileName(generated.fileName());
            asset.setStorageKey(storageKey);
            asset.setMimeType(generated.contentType());
            asset.setFileSizeBytes(stored.sizeBytes());
            asset.setChecksumSha256(stored.checksumSha256());
            asset = fileAssetRepository.saveAndFlush(asset);
            for (var candidate : generated.missingCvCandidates()) {
                ExportFileEntity missing = new ExportFileEntity();
                missing.setExportJobId(job.getId());
                missing.setStudentId(candidate.studentId());
                missing.setIndexNumber(candidate.indexNumber());
                missing.setFullName(candidate.fullName());
                missingRepository.save(missing);
            }
            if (!generated.missingCvCandidates().isEmpty()) {
                saveWarning(job.getId(), ExportWarningCode.MISSING_CVS,
                        "One or more candidates did not have an available saved CV.");
                saveWarning(job.getId(), ExportWarningCode.PARTIAL_EXPORT,
                        "The archive contains only candidates with an available saved CV.");
            }
            OffsetDateTime completedAt = OffsetDateTime.now(clock);
            job.setFileAssetId(asset.getId());
            job.setTotalCandidateCount(generated.totalCandidateCount());
            job.setIncludedFileCount(generated.includedFileCount());
            job.setMissingCvCount(generated.missingCvCandidates().size());
            job.setStatus(ExportStatus.COMPLETED);
            job.setCompletedAt(completedAt);
            job.setExpiresAt(completedAt.plus(properties.processing().retention()));
            jobRepository.saveAndFlush(job);
            auditEventPublisher.recordRequired(
                    job.getRequestedByAccountId(), RoleName.ADMIN.name(), AuditEventType.EXPORT_JOB_COMPLETED.name(),
                    AuditEventCategory.EXPORT_MANAGEMENT, AUDIT_RESOURCE, job.getId().toString(),
                    Map.of("includedFileCount", generated.includedFileCount(),
                            "missingCvCount", generated.missingCvCandidates().size()));
        } catch (RuntimeException exception) {
            try {
                fileStorage.delete(storageKey);
            } catch (RuntimeException ignored) {
                // Preserve the transaction failure.
            }
            throw exception;
        }
    }

    @Transactional
    public void fail(UUID exportJobId, String code, String message) {
        ExportJobEntity job = requireJob(exportJobId);
        if (job.getStatus() == ExportStatus.COMPLETED || job.getStatus() == ExportStatus.CANCELLED) {
            return;
        }
        job.setStatus(ExportStatus.FAILED);
        job.setFailureCode(code);
        String safeMessage = message == null ? "Export generation failed." : message;
        job.setFailureMessage(safeMessage.substring(0, Math.min(500, safeMessage.length())));
        job.setCompletedAt(OffsetDateTime.now(clock));
        jobRepository.saveAndFlush(job);
        auditEventPublisher.recordRequired(
                job.getRequestedByAccountId(), RoleName.ADMIN.name(), AuditEventType.EXPORT_JOB_FAILED.name(),
                AuditEventCategory.EXPORT_MANAGEMENT, AUDIT_RESOURCE, job.getId().toString(),
                Map.of("failureCode", code));
    }

    private void saveWarning(UUID exportJobId, ExportWarningCode code, String message) {
        ExportWarningEntity warning = new ExportWarningEntity();
        warning.setExportJobId(exportJobId);
        warning.setWarningCode(code);
        warning.setMessage(message);
        warningRepository.save(warning);
    }

    private ExportJobResponse response(ExportJobEntity job) {
        return mapper.toResponse(
                job,
                missingRepository.findAllByExportJobIdOrderByIndexNumberAscStudentIdAsc(job.getId()),
                warningRepository.findAllByExportJobIdOrderByWarningCodeAsc(job.getId()));
    }

    private ExportJobEntity requireJob(UUID exportJobId) {
        return jobRepository.findById(exportJobId)
                .orElseThrow(() -> new NotFoundException("Export job was not found."));
    }

    private void validateFormat(ExportType type, ExportFormat format) {
        ExportFormat expected = type == ExportType.SHORTLIST_SUMMARY_CSV ? ExportFormat.CSV : ExportFormat.ZIP;
        if (format != expected) {
            throw new ValidationException("The export format is not valid for this export type.");
        }
    }

    private CurrentActor currentAdmin() {
        CurrentActor actor = currentActorProvider.currentActor()
                .orElseThrow(() -> new UnauthorizedException("Authentication is required."));
        if (!actor.hasRole(RoleName.ADMIN)) {
            throw new ForbiddenException("The current account cannot manage exports.");
        }
        return actor;
    }
}
