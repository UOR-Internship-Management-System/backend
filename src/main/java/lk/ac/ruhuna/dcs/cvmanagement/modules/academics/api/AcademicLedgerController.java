package lk.ac.ruhuna.dcs.cvmanagement.modules.academics.api;

import java.net.URI;
import jakarta.validation.Valid;
import java.util.UUID;
import lk.ac.ruhuna.dcs.cvmanagement.modules.academics.api.dto.request.AcademicLedgerCommitRequest;
import lk.ac.ruhuna.dcs.cvmanagement.modules.academics.api.dto.response.AcademicLedgerCommitResponse;
import lk.ac.ruhuna.dcs.cvmanagement.modules.academics.api.dto.response.AcademicLedgerStagedRowResponse;
import lk.ac.ruhuna.dcs.cvmanagement.modules.academics.api.dto.response.AcademicLedgerUploadDetailResponse;
import lk.ac.ruhuna.dcs.cvmanagement.modules.academics.api.dto.response.AcademicLedgerUploadSummaryResponse;
import lk.ac.ruhuna.dcs.cvmanagement.modules.academics.api.dto.response.AcademicLedgerValidationResultResponse;
import lk.ac.ruhuna.dcs.cvmanagement.modules.academics.application.AcademicLedgerCommitService;
import lk.ac.ruhuna.dcs.cvmanagement.modules.academics.application.AcademicLedgerReviewService;
import lk.ac.ruhuna.dcs.cvmanagement.modules.academics.application.AcademicLedgerUploadService;
import lk.ac.ruhuna.dcs.cvmanagement.modules.academics.config.AcademicLedgerProperties;
import lk.ac.ruhuna.dcs.cvmanagement.shared.api.ApiPaths;
import lk.ac.ruhuna.dcs.cvmanagement.shared.pagination.dto.PagedResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/** Admin Academic Ledger upload, polling, staged-review, and validation-result endpoints. */
@RestController
@RequestMapping(ApiPaths.ADMIN_ACADEMIC_LEDGER_UPLOADS)
public class AcademicLedgerController {

    private final AcademicLedgerUploadService uploadService;
    private final AcademicLedgerCommitService commitService;
    private final AcademicLedgerReviewService reviewService;
    private final AcademicLedgerProperties properties;

    public AcademicLedgerController(
            AcademicLedgerUploadService uploadService,
            AcademicLedgerCommitService commitService,
            AcademicLedgerReviewService reviewService,
            AcademicLedgerProperties properties) {
        this.uploadService = uploadService;
        this.commitService = commitService;
        this.reviewService = reviewService;
        this.properties = properties;
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<AcademicLedgerUploadDetailResponse> upload(
            @RequestPart(value = "file", required = false) MultipartFile file) {
        AcademicLedgerUploadDetailResponse response = uploadService.upload(file);
        URI location = URI.create(ApiPaths.ADMIN_ACADEMIC_LEDGER_UPLOADS + "/" + response.uploadId());
        return ResponseEntity.accepted()
                .location(location)
                .header(HttpHeaders.RETRY_AFTER, Integer.toString(properties.retryAfterSeconds()))
                .body(response);
    }

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public PagedResponse<AcademicLedgerUploadSummaryResponse> list(
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size,
            @RequestParam(required = false) String sort,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String validationStatus) {
        return uploadService.listUploads(page, size, sort, search, status, validationStatus);
    }

    @GetMapping(value = "/{uploadId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public AcademicLedgerUploadDetailResponse get(@PathVariable UUID uploadId) {
        return uploadService.getUpload(uploadId);
    }

    @PostMapping(value = "/{uploadId}/commit", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public AcademicLedgerCommitResponse commit(
            @PathVariable UUID uploadId, @Valid @RequestBody AcademicLedgerCommitRequest request) {
        return commitService.commit(uploadId);
    }

    @GetMapping(value = "/{uploadId}/staged-rows", produces = MediaType.APPLICATION_JSON_VALUE)
    public PagedResponse<AcademicLedgerStagedRowResponse> stagedRows(
            @PathVariable UUID uploadId,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size,
            @RequestParam(required = false) String sort,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String validationStatus) {
        return reviewService.listStagedRows(uploadId, page, size, sort, search, validationStatus);
    }

    @GetMapping(value = "/{uploadId}/validation-results", produces = MediaType.APPLICATION_JSON_VALUE)
    public AcademicLedgerValidationResultResponse validationResults(@PathVariable UUID uploadId) {
        return reviewService.getValidation(uploadId);
    }
}
