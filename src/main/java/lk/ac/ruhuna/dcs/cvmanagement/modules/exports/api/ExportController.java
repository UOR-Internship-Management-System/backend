package lk.ac.ruhuna.dcs.cvmanagement.modules.exports.api;

import jakarta.validation.Valid;
import java.net.URI;
import java.util.UUID;
import lk.ac.ruhuna.dcs.cvmanagement.modules.exports.api.dto.request.BulkCvExportCreateRequest;
import lk.ac.ruhuna.dcs.cvmanagement.modules.exports.api.dto.request.ShortlistSummaryExportCreateRequest;
import lk.ac.ruhuna.dcs.cvmanagement.modules.exports.api.dto.response.ExportFileResponse;
import lk.ac.ruhuna.dcs.cvmanagement.modules.exports.api.dto.response.ExportJobResponse;
import lk.ac.ruhuna.dcs.cvmanagement.modules.exports.application.ExportJobService;
import lk.ac.ruhuna.dcs.cvmanagement.modules.exports.domain.policy.ExportType;
import lk.ac.ruhuna.dcs.cvmanagement.shared.api.ApiPaths;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.CacheControl;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Admin-only HTTP boundary for asynchronous shortlist exports. */
@RestController
@RequestMapping(ApiPaths.ADMIN_EXPORTS)
public class ExportController {
    private final ExportJobService exportJobService;

    public ExportController(ExportJobService exportJobService) {
        this.exportJobService = exportJobService;
    }

    @PostMapping(path = "/shortlists/{shortlistId}", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ExportJobResponse> createSummary(@PathVariable UUID shortlistId, @Valid @RequestBody ShortlistSummaryExportCreateRequest request) {
        return accepted(exportJobService.create(shortlistId, ExportType.SHORTLIST_SUMMARY_CSV, request.format()));
    }

    @PostMapping(path = "/shortlists/{shortlistId}/bulk-cvs", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ExportJobResponse> createBulkCvs(@PathVariable UUID shortlistId, @Valid @RequestBody BulkCvExportCreateRequest request) {
        return accepted(exportJobService.create(shortlistId, ExportType.BULK_LATEST_CV_ZIP, request.format()));
    }

    @GetMapping(path = "/{exportJobId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ExportJobResponse get(@PathVariable UUID exportJobId) {
        return exportJobService.get(exportJobId);
    }

    @GetMapping(path = "/{exportJobId}/download", produces = "text/csv")
    public ResponseEntity<InputStreamResource> downloadSummary(@PathVariable UUID exportJobId) {
        return download(exportJobService.download(exportJobId, ExportType.SHORTLIST_SUMMARY_CSV));
    }

    @GetMapping(path = "/{exportJobId}/bulk-cvs/download", produces = "application/zip")
    public ResponseEntity<InputStreamResource> downloadBulkCvs(@PathVariable UUID exportJobId) {
        return download(exportJobService.download(exportJobId, ExportType.BULK_LATEST_CV_ZIP));
    }

    private ResponseEntity<ExportJobResponse> accepted(ExportJobResponse response) {
        return ResponseEntity.accepted().location(URI.create(ApiPaths.ADMIN_EXPORTS + "/" + response.exportJobId())).header(HttpHeaders.RETRY_AFTER, "2").body(response);
    }

    private ResponseEntity<InputStreamResource> download(ExportFileResponse file) {
        return ResponseEntity.ok()
                .cacheControl(CacheControl.noStore())
                .contentType(MediaType.parseMediaType(file.contentType()))
                .contentLength(file.fileSizeBytes())
                .header(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.attachment().filename(file.fileName()).build().toString())
                .body(new InputStreamResource(file.content()));
    }
}
