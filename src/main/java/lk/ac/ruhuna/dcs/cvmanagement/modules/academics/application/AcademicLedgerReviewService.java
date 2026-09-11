package lk.ac.ruhuna.dcs.cvmanagement.modules.academics.application;

import java.util.Collection;
import java.util.EnumSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import lk.ac.ruhuna.dcs.cvmanagement.modules.academics.api.dto.response.AcademicLedgerStagedRowResponse;
import lk.ac.ruhuna.dcs.cvmanagement.modules.academics.api.dto.response.AcademicLedgerValidationErrorResponse;
import lk.ac.ruhuna.dcs.cvmanagement.modules.academics.api.dto.response.AcademicLedgerValidationResultResponse;
import lk.ac.ruhuna.dcs.cvmanagement.modules.academics.api.error.AcademicLedgerErrors;
import lk.ac.ruhuna.dcs.cvmanagement.modules.academics.domain.AcademicLedgerRowValidationStatus;
import lk.ac.ruhuna.dcs.cvmanagement.modules.academics.domain.AcademicLedgerStagedRowSort;
import lk.ac.ruhuna.dcs.cvmanagement.modules.academics.domain.AcademicLedgerUploadStatus;
import lk.ac.ruhuna.dcs.cvmanagement.modules.academics.domain.AcademicLedgerValidationStatus;
import lk.ac.ruhuna.dcs.cvmanagement.modules.academics.mapper.AcademicMapper;
import lk.ac.ruhuna.dcs.cvmanagement.modules.academics.persistence.entity.AcademicLedgerStagingRowEntity;
import lk.ac.ruhuna.dcs.cvmanagement.modules.academics.persistence.entity.AcademicLedgerValidationErrorEntity;
import lk.ac.ruhuna.dcs.cvmanagement.modules.academics.persistence.repository.AcademicLedgerStagingRowRepository;
import lk.ac.ruhuna.dcs.cvmanagement.modules.academics.persistence.repository.AcademicLedgerUploadRepository;
import lk.ac.ruhuna.dcs.cvmanagement.modules.academics.persistence.repository.AcademicLedgerValidationErrorRepository;
import lk.ac.ruhuna.dcs.cvmanagement.shared.pagination.dto.PagedResponse;
import lk.ac.ruhuna.dcs.cvmanagement.shared.security.CurrentActorProvider;
import lk.ac.ruhuna.dcs.cvmanagement.shared.security.RoleName;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Read-only Admin review APIs for staged rows and validation diagnostics. */
@Service
public class AcademicLedgerReviewService {

    private static final Collection<AcademicLedgerUploadStatus> REVIEWABLE_STATUSES = EnumSet.of(
            AcademicLedgerUploadStatus.STAGED,
            AcademicLedgerUploadStatus.READY_TO_COMMIT,
            AcademicLedgerUploadStatus.VALIDATION_FAILED,
            AcademicLedgerUploadStatus.COMMITTING,
            AcademicLedgerUploadStatus.COMMITTED);

    private final AcademicLedgerUploadRepository uploadRepository;
    private final AcademicLedgerStagingRowRepository stagingRepository;
    private final AcademicLedgerValidationErrorRepository validationErrorRepository;
    private final AcademicMapper mapper;
    private final CurrentActorProvider currentActorProvider;

    public AcademicLedgerReviewService(
            AcademicLedgerUploadRepository uploadRepository,
            AcademicLedgerStagingRowRepository stagingRepository,
            AcademicLedgerValidationErrorRepository validationErrorRepository,
            AcademicMapper mapper,
            CurrentActorProvider currentActorProvider) {
        this.uploadRepository = uploadRepository;
        this.stagingRepository = stagingRepository;
        this.validationErrorRepository = validationErrorRepository;
        this.mapper = mapper;
        this.currentActorProvider = currentActorProvider;
    }

    @Transactional(readOnly = true)
    public PagedResponse<AcademicLedgerStagedRowResponse> listStagedRows(
            UUID uploadId, Integer page, Integer size, String sort, String search, String validationStatus) {
        requireAdmin();
        var upload = uploadRepository.findById(uploadId).orElseThrow(AcademicLedgerErrors::uploadNotFound);
        if (!REVIEWABLE_STATUSES.contains(upload.getUploadStatus())) {
            throw AcademicLedgerErrors.notReady("Staged rows are not available for the current upload state.");
        }
        if (upload.getUploadStatus() == AcademicLedgerUploadStatus.STAGED
                && (upload.getValidationStatus() == AcademicLedgerValidationStatus.NOT_STARTED
                        || upload.getValidationStatus() == AcademicLedgerValidationStatus.IN_PROGRESS)) {
            throw AcademicLedgerErrors.notReady("Staged-row validation is still in progress.");
        }
        int safePage = page == null ? 0 : page;
        int safeSize = size == null ? 20 : size;
        if (safePage < 0) {
            throw AcademicLedgerErrors.badRequest("page must be greater than or equal to 0.");
        }
        if (safeSize < 1 || safeSize > 100) {
            throw AcademicLedgerErrors.badRequest("size must be between 1 and 100.");
        }
        String safeSearch = normalizeSearch(search);
        AcademicLedgerRowValidationStatus rowStatus = parseRowValidationStatus(validationStatus);
        AcademicLedgerStagedRowSort safeSort = AcademicLedgerStagedRowSort.fromApiValue(sort);
        Page<AcademicLedgerStagingRowEntity> rows = stagingRepository.findAll(
                stagedRowSpecification(uploadId, safeSearch, rowStatus),
                PageRequest.of(safePage, safeSize, safeSort.sort()));

        List<UUID> rowIds = rows.getContent().stream().map(AcademicLedgerStagingRowEntity::getId).toList();
        Map<UUID, List<AcademicLedgerValidationErrorEntity>> errorsByRow = rowIds.isEmpty()
                ? Map.of()
                : validationErrorRepository.findByStagingRowIdInOrderByCreatedAtAsc(rowIds).stream()
                        .collect(Collectors.groupingBy(AcademicLedgerValidationErrorEntity::getStagingRowId));
        Page<AcademicLedgerStagedRowResponse> mapped = rows.map(row -> mapper.toStagedRow(
                row, errorsByRow.getOrDefault(row.getId(), List.of())));
        return PagedResponse.of(mapped, safeSort.apiValue());
    }

    private Specification<AcademicLedgerStagingRowEntity> stagedRowSpecification(
            UUID uploadId,
            String search,
            AcademicLedgerRowValidationStatus validationStatus) {
        Specification<AcademicLedgerStagingRowEntity> specification =
                (root, query, criteriaBuilder) ->
                        criteriaBuilder.equal(root.get("academicLedgerUploadId"), uploadId);
        if (search != null) {
            String pattern = "%" + search + "%";
            specification = specification.and((root, query, criteriaBuilder) -> criteriaBuilder.or(
                    criteriaBuilder.like(criteriaBuilder.lower(root.get("studentIndexNumber")), pattern),
                    criteriaBuilder.like(criteriaBuilder.lower(root.get("courseCode")), pattern),
                    criteriaBuilder.like(criteriaBuilder.lower(root.get("courseTitle")), pattern)));
        }
        if (validationStatus != null) {
            specification = specification.and((root, query, criteriaBuilder) ->
                    criteriaBuilder.equal(root.get("validationStatus"), validationStatus));
        }
        return specification;
    }

    @Transactional(readOnly = true)
    public AcademicLedgerValidationResultResponse getValidation(UUID uploadId) {
        requireAdmin();
        var upload = uploadRepository.findById(uploadId).orElseThrow(AcademicLedgerErrors::uploadNotFound);
        if (upload.getValidationStatus() == AcademicLedgerValidationStatus.NOT_STARTED
                || upload.getValidationStatus() == AcademicLedgerValidationStatus.IN_PROGRESS) {
            throw AcademicLedgerErrors.notReady("Validation results are not complete for this upload.");
        }
        List<AcademicLedgerValidationErrorResponse> errors = validationErrorRepository.findValidationResultRows(uploadId)
                .stream()
                .map(mapper::toValidationError)
                .toList();
        boolean valid = upload.getValidationStatus() == AcademicLedgerValidationStatus.PASSED
                && upload.getInvalidRows() == 0;
        return new AcademicLedgerValidationResultResponse(
                upload.getId(), upload.getValidationStatus(), valid,
                upload.getTotalRows(), upload.getValidRows(), upload.getInvalidRows(), errors);
    }

    private void requireAdmin() {
        var actor = currentActorProvider.currentActor().orElseThrow(AcademicLedgerErrors::unauthorized);
        if (!actor.hasRole(RoleName.ADMIN)) {
            throw AcademicLedgerErrors.forbidden();
        }
    }

    private String normalizeSearch(String search) {
        if (search == null) {
            return null;
        }
        String value = search.trim();
        if (value.isEmpty() || value.length() > 120) {
            throw AcademicLedgerErrors.badRequest("search must contain between 1 and 120 characters.");
        }
        return value.toLowerCase(Locale.ROOT);
    }

    private AcademicLedgerRowValidationStatus parseRowValidationStatus(String value) {
        if (value == null) {
            return null;
        }
        if (value.isBlank()) {
            throw AcademicLedgerErrors.badRequest("validationStatus must not be blank when supplied.");
        }
        try {
            return AcademicLedgerRowValidationStatus.valueOf(value);
        } catch (IllegalArgumentException exception) {
            throw AcademicLedgerErrors.badRequest("Unsupported staged-row validation status.");
        }
    }
}
