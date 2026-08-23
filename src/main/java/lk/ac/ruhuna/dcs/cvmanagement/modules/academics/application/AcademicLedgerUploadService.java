package lk.ac.ruhuna.dcs.cvmanagement.modules.academics.application;

import java.io.IOException;
import java.io.InputStream;
import java.time.Clock;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Collection;
import java.util.EnumSet;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
import lk.ac.ruhuna.dcs.cvmanagement.infrastructure.storage.FileAssetEntity;
import lk.ac.ruhuna.dcs.cvmanagement.infrastructure.storage.FileAssetRepository;
import lk.ac.ruhuna.dcs.cvmanagement.infrastructure.storage.FileStorageException;
import lk.ac.ruhuna.dcs.cvmanagement.infrastructure.storage.FileStoragePort;
import lk.ac.ruhuna.dcs.cvmanagement.modules.academics.api.dto.response.AcademicLedgerUploadDetailResponse;
import lk.ac.ruhuna.dcs.cvmanagement.modules.academics.api.dto.response.AcademicLedgerUploadSummaryResponse;
import lk.ac.ruhuna.dcs.cvmanagement.modules.academics.api.error.AcademicLedgerErrors;
import lk.ac.ruhuna.dcs.cvmanagement.modules.academics.config.AcademicLedgerProperties;
import lk.ac.ruhuna.dcs.cvmanagement.modules.academics.domain.AcademicLedgerUploadSort;
import lk.ac.ruhuna.dcs.cvmanagement.modules.academics.domain.AcademicLedgerUploadStatus;
import lk.ac.ruhuna.dcs.cvmanagement.modules.academics.domain.AcademicLedgerValidationStatus;
import lk.ac.ruhuna.dcs.cvmanagement.modules.academics.persistence.entity.AcademicLedgerUploadEntity;
import lk.ac.ruhuna.dcs.cvmanagement.modules.academics.persistence.repository.AcademicLedgerUploadRepository;
import lk.ac.ruhuna.dcs.cvmanagement.shared.audit.AuditEventCategory;
import lk.ac.ruhuna.dcs.cvmanagement.shared.audit.AuditEventPublisher;
import lk.ac.ruhuna.dcs.cvmanagement.shared.pagination.dto.PagedResponse;
import lk.ac.ruhuna.dcs.cvmanagement.shared.security.CurrentActor;
import lk.ac.ruhuna.dcs.cvmanagement.shared.security.CurrentActorProvider;
import lk.ac.ruhuna.dcs.cvmanagement.shared.security.RoleName;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.multipart.MultipartFile;

/** Coordinates safe Academic Ledger upload acceptance and read-only upload-history queries. */
@Service
public class AcademicLedgerUploadService {

    private static final Logger LOGGER = LoggerFactory.getLogger(AcademicLedgerUploadService.class);
    private static final String CSV_CONTENT_TYPE = "text/csv";
    private static final String AUDIT_EVENT = "LEDGER_UPLOAD_ACCEPTED";
    private static final String AUDIT_RESOURCE = "ACADEMIC_LEDGER_UPLOAD";

    private static final Collection<AcademicLedgerUploadStatus> DUPLICATE_PROTECTED_STATUSES =
            EnumSet.of(
                    AcademicLedgerUploadStatus.RECEIVED,
                    AcademicLedgerUploadStatus.PROCESSING,
                    AcademicLedgerUploadStatus.STAGED,
                    AcademicLedgerUploadStatus.READY_TO_COMMIT,
                    AcademicLedgerUploadStatus.COMMITTING,
                    AcademicLedgerUploadStatus.COMMITTED);

    private final AcademicLedgerUploadPreflightValidator preflightValidator;
    private final AcademicLedgerUploadRepository uploadRepository;
    private final FileAssetRepository fileAssetRepository;
    private final FileStoragePort fileStorage;
    private final AcademicLedgerProperties properties;
    private final CurrentActorProvider currentActorProvider;
    private final AuditEventPublisher auditEventPublisher;
    private final Clock clock;
    private final TransactionTemplate transactionTemplate;

    public AcademicLedgerUploadService(
            AcademicLedgerUploadPreflightValidator preflightValidator,
            AcademicLedgerUploadRepository uploadRepository,
            FileAssetRepository fileAssetRepository,
            FileStoragePort fileStorage,
            AcademicLedgerProperties properties,
            CurrentActorProvider currentActorProvider,
            AuditEventPublisher auditEventPublisher,
            Clock clock,
            PlatformTransactionManager transactionManager) {
        this.preflightValidator = preflightValidator;
        this.uploadRepository = uploadRepository;
        this.fileAssetRepository = fileAssetRepository;
        this.fileStorage = fileStorage;
        this.properties = properties;
        this.currentActorProvider = currentActorProvider;
        this.auditEventPublisher = auditEventPublisher;
        this.clock = clock;
        this.transactionTemplate = new TransactionTemplate(transactionManager);
    }

    public AcademicLedgerUploadDetailResponse upload(MultipartFile file) {
        CurrentActor actor = currentAdmin();
        var validated = preflightValidator.validate(file);

        findProtectedDuplicate(validated.checksumSha256())
                .ifPresent(existing -> {
                    throw AcademicLedgerErrors.duplicateUpload(existing.getId());
                });

        String storageKey = newStorageKey();
        FileStoragePort.StoredFile storedFile;
        try (InputStream input = file.getInputStream()) {
            storedFile = fileStorage.store(storageKey, input);
        } catch (IOException | FileStorageException exception) {
            throw AcademicLedgerErrors.storageUnavailable();
        }

        if (storedFile.sizeBytes() != validated.sizeBytes()
                || !storedFile.checksumSha256().equals(validated.checksumSha256())) {
            deleteStoredFileQuietly(storageKey);
            LOGGER.error("Academic Ledger source changed between preflight and durable storage.");
            throw AcademicLedgerErrors.internalFailure();
        }

        PersistedAcceptance persisted;
        try {
            persisted = persistAcceptedUpload(actor, validated, storageKey);
        } catch (DataIntegrityViolationException exception) {
            deleteStoredFileQuietly(storageKey);
            var existing = findProtectedDuplicate(validated.checksumSha256());
            if (existing.isPresent()) {
                throw AcademicLedgerErrors.duplicateUpload(existing.get().getId());
            }
            LOGGER.error("Academic Ledger upload persistence violated a database constraint.", exception);
            throw AcademicLedgerErrors.internalFailure();
        } catch (RuntimeException exception) {
            deleteStoredFileQuietly(storageKey);
            throw exception;
        }
        return toDetail(persisted.upload(), persisted.asset());
    }

    @Transactional(readOnly = true)
    public PagedResponse<AcademicLedgerUploadSummaryResponse> listUploads(
            Integer page,
            Integer size,
            String sort,
            String search,
            String status,
            String validationStatus) {
        currentAdmin();
        int safePage = validatePage(page);
        int safeSize = validateSize(size);
        AcademicLedgerUploadSort safeSort = AcademicLedgerUploadSort.fromApiValue(sort);
        String safeSearch = validateSearch(search);
        AcademicLedgerUploadStatus uploadStatus = parseUploadStatus(status);
        AcademicLedgerValidationStatus validation = parseValidationStatus(validationStatus);

        PageRequest pageable = PageRequest.of(safePage, safeSize, safeSort.sort());
        Page<AcademicLedgerUploadEntity> uploads = uploadRepository.findAll(
                uploadSpecification(safeSearch, uploadStatus, validation), pageable);

        Map<UUID, FileAssetEntity> assets = fileAssetRepository.findAllById(
                        uploads.getContent().stream().map(AcademicLedgerUploadEntity::getSourceFileAssetId).toList())
                .stream()
                .collect(Collectors.toUnmodifiableMap(
                        FileAssetEntity::getId, Function.identity(), (first, ignored) -> first));

        Page<AcademicLedgerUploadSummaryResponse> mapped = uploads.map(upload ->
                toSummary(upload, requiredAsset(assets, upload.getSourceFileAssetId())));
        return PagedResponse.of(mapped, safeSort.apiValue());
    }

    private Specification<AcademicLedgerUploadEntity> uploadSpecification(
            String search,
            AcademicLedgerUploadStatus uploadStatus,
            AcademicLedgerValidationStatus validationStatus) {
        Specification<AcademicLedgerUploadEntity> specification =
                (root, query, criteriaBuilder) -> criteriaBuilder.conjunction();
        if (search != null) {
            String pattern = "%" + search + "%";
            specification = specification.and((root, query, criteriaBuilder) ->
                    criteriaBuilder.like(criteriaBuilder.lower(root.get("fileName")), pattern));
        }
        if (uploadStatus != null) {
            specification = specification.and((root, query, criteriaBuilder) ->
                    criteriaBuilder.equal(root.get("uploadStatus"), uploadStatus));
        }
        if (validationStatus != null) {
            specification = specification.and((root, query, criteriaBuilder) ->
                    criteriaBuilder.equal(root.get("validationStatus"), validationStatus));
        }
        return specification;
    }

    @Transactional(readOnly = true)
    public AcademicLedgerUploadDetailResponse getUpload(UUID uploadId) {
        currentAdmin();
        AcademicLedgerUploadEntity upload = uploadRepository.findById(uploadId)
                .orElseThrow(AcademicLedgerErrors::uploadNotFound);
        FileAssetEntity asset = fileAssetRepository.findById(upload.getSourceFileAssetId())
                .orElseThrow(AcademicLedgerErrors::internalFailure);
        return toDetail(upload, asset);
    }

    private PersistedAcceptance persistAcceptedUpload(
            CurrentActor actor,
            AcademicLedgerUploadPreflightValidator.ValidatedLedgerFile validated,
            String storageKey) {
        PersistedAcceptance result = transactionTemplate.execute(status -> {
            FileAssetEntity asset = new FileAssetEntity();
            asset.setOwnerAccountId(actor.userId());
            asset.setFileName(validated.originalFilename());
            asset.setStorageKey(storageKey);
            asset.setMimeType(CSV_CONTENT_TYPE);
            asset.setFileSizeBytes(validated.sizeBytes());
            asset.setChecksumSha256(validated.checksumSha256());
            asset = fileAssetRepository.saveAndFlush(asset);

            AcademicLedgerUploadEntity upload = new AcademicLedgerUploadEntity();
            upload.setUploadedByAccountId(actor.userId());
            upload.setSourceFileAssetId(asset.getId());
            upload.setFileName(validated.originalFilename());
            upload.setFileHash(validated.checksumSha256());
            upload.setUploadStatus(AcademicLedgerUploadStatus.RECEIVED);
            upload.setValidationStatus(AcademicLedgerValidationStatus.NOT_STARTED);
            upload = uploadRepository.saveAndFlush(upload);

            auditEventPublisher.recordRequired(
                    actor.userId(),
                    RoleName.ADMIN.name(),
                    AUDIT_EVENT,
                    AuditEventCategory.ACADEMIC_LEDGER,
                    AUDIT_RESOURCE,
                    upload.getId().toString(),
                    Map.of(
                            "uploadId", upload.getId().toString(),
                            "originalFilename", validated.originalFilename(),
                            "fileSizeBytes", validated.sizeBytes()));
            return new PersistedAcceptance(upload, asset);
        });
        return Objects.requireNonNull(result, "Upload transaction returned no result.");
    }

    private java.util.Optional<AcademicLedgerUploadEntity> findProtectedDuplicate(String checksumSha256) {
        return uploadRepository.findFirstByFileHashAndUploadStatusIn(
                checksumSha256, DUPLICATE_PROTECTED_STATUSES);
    }

    private AcademicLedgerUploadSummaryResponse toSummary(
            AcademicLedgerUploadEntity upload, FileAssetEntity asset) {
        return new AcademicLedgerUploadSummaryResponse(
                upload.getId(),
                upload.getFileName(),
                asset.getMimeType(),
                asset.getFileSizeBytes(),
                upload.getUploadStatus(),
                upload.getValidationStatus(),
                upload.getTotalRows(),
                upload.getValidRows(),
                upload.getInvalidRows(),
                upload.getCreatedAt(),
                upload.getCommittedAt(),
                upload.getFailureSummary());
    }

    private AcademicLedgerUploadDetailResponse toDetail(
            AcademicLedgerUploadEntity upload, FileAssetEntity asset) {
        return new AcademicLedgerUploadDetailResponse(
                upload.getId(),
                upload.getFileName(),
                asset.getMimeType(),
                asset.getFileSizeBytes(),
                upload.getUploadStatus(),
                upload.getValidationStatus(),
                upload.getTotalRows(),
                upload.getValidRows(),
                upload.getInvalidRows(),
                upload.getCreatedAt(),
                upload.getCommittedAt(),
                upload.getFailureSummary(),
                statusMessage(upload.getUploadStatus()),
                shouldPoll(upload.getUploadStatus()) ? properties.retryAfterSeconds() : null);
    }

    private FileAssetEntity requiredAsset(Map<UUID, FileAssetEntity> assets, UUID id) {
        FileAssetEntity asset = assets.get(id);
        if (asset == null) {
            throw AcademicLedgerErrors.internalFailure();
        }
        return asset;
    }

    private String statusMessage(AcademicLedgerUploadStatus status) {
        return switch (status) {
            case RECEIVED -> "The CSV file was accepted and is waiting for processing.";
            case PROCESSING -> "The CSV file is being parsed and staged.";
            case STAGED -> "The CSV rows are staged and validation is in progress.";
            case READY_TO_COMMIT -> "All staged rows passed validation and the batch is ready to commit.";
            case COMMITTING -> "The validated academic ledger is being committed.";
            case COMMITTED -> "The academic ledger was committed successfully.";
            case VALIDATION_FAILED -> "Validation failed. Review the validation results and upload a corrected CSV.";
            case PROCESSING_FAILED -> "The academic ledger could not be processed.";
        };
    }

    private boolean shouldPoll(AcademicLedgerUploadStatus status) {
        return status == AcademicLedgerUploadStatus.RECEIVED
                || status == AcademicLedgerUploadStatus.PROCESSING
                || status == AcademicLedgerUploadStatus.STAGED
                || status == AcademicLedgerUploadStatus.COMMITTING;
    }

    private CurrentActor currentAdmin() {
        CurrentActor actor = currentActorProvider.currentActor()
                .orElseThrow(AcademicLedgerErrors::unauthorized);
        if (!actor.hasRole(RoleName.ADMIN)) {
            throw AcademicLedgerErrors.forbidden();
        }
        return actor;
    }

    private int validatePage(Integer page) {
        int value = page == null ? 0 : page;
        if (value < 0) {
            throw AcademicLedgerErrors.badRequest("page must be greater than or equal to 0.");
        }
        return value;
    }

    private int validateSize(Integer size) {
        int value = size == null ? 20 : size;
        if (value < 1 || value > 100) {
            throw AcademicLedgerErrors.badRequest("size must be between 1 and 100.");
        }
        return value;
    }

    private String validateSearch(String search) {
        if (search == null) {
            return null;
        }
        String value = search.trim();
        if (value.isEmpty() || value.length() > 120) {
            throw AcademicLedgerErrors.badRequest("search must contain between 1 and 120 characters.");
        }
        return value.toLowerCase(Locale.ROOT);
    }

    private AcademicLedgerUploadStatus parseUploadStatus(String status) {
        if (status == null) {
            return null;
        }
        if (status.isBlank()) {
            throw AcademicLedgerErrors.badRequest("status must not be blank when supplied.");
        }
        try {
            return AcademicLedgerUploadStatus.valueOf(status);
        } catch (IllegalArgumentException exception) {
            throw AcademicLedgerErrors.badRequest("Unsupported Academic Ledger upload status.");
        }
    }

    private AcademicLedgerValidationStatus parseValidationStatus(String status) {
        if (status == null) {
            return null;
        }
        if (status.isBlank()) {
            throw AcademicLedgerErrors.badRequest("validationStatus must not be blank when supplied.");
        }
        try {
            return AcademicLedgerValidationStatus.valueOf(status);
        } catch (IllegalArgumentException exception) {
            throw AcademicLedgerErrors.badRequest("Unsupported Academic Ledger validation status.");
        }
    }

    private String newStorageKey() {
        OffsetDateTime now = OffsetDateTime.now(clock).withOffsetSameInstant(ZoneOffset.UTC);
        return "academic-ledger/%04d/%02d/%s.csv".formatted(
                now.getYear(), now.getMonthValue(), UUID.randomUUID());
    }

    private record PersistedAcceptance(AcademicLedgerUploadEntity upload, FileAssetEntity asset) {
    }

    private void deleteStoredFileQuietly(String storageKey) {
        try {
            fileStorage.delete(storageKey);
        } catch (RuntimeException cleanupFailure) {
            LOGGER.warn("Failed to remove an unreferenced Academic Ledger source file after rollback.");
        }
    }
}
