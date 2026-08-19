package lk.ac.ruhuna.dcs.cvmanagement.modules.cv.api;

import jakarta.validation.Valid;
import lk.ac.ruhuna.dcs.cvmanagement.modules.cv.api.dto.request.CvPreviewRequest;
import lk.ac.ruhuna.dcs.cvmanagement.modules.cv.api.dto.request.CvSaveRequest;
import lk.ac.ruhuna.dcs.cvmanagement.modules.cv.api.dto.response.CvFreshnessResponse;
import lk.ac.ruhuna.dcs.cvmanagement.modules.cv.api.dto.response.CvPreviewResponse;
import lk.ac.ruhuna.dcs.cvmanagement.modules.cv.api.dto.response.CvResponse;
import lk.ac.ruhuna.dcs.cvmanagement.modules.cv.application.CvDownloadService;
import lk.ac.ruhuna.dcs.cvmanagement.modules.cv.application.CvFreshnessService;
import lk.ac.ruhuna.dcs.cvmanagement.modules.cv.application.CvPreviewService;
import lk.ac.ruhuna.dcs.cvmanagement.modules.cv.application.CvSaveService;
import lk.ac.ruhuna.dcs.cvmanagement.shared.api.ApiPaths;
import lk.ac.ruhuna.dcs.cvmanagement.shared.http.IfMatchSupport;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class CvController {

    private final CvFreshnessService freshnessService;
    private final CvPreviewService previewService;
    private final CvSaveService saveService;
    private final CvDownloadService downloadService;

    public CvController(
            CvFreshnessService freshnessService,
            CvPreviewService previewService,
            CvSaveService saveService,
            CvDownloadService downloadService) {
        this.freshnessService = freshnessService;
        this.previewService = previewService;
        this.saveService = saveService;
        this.downloadService = downloadService;
    }

    @GetMapping(ApiPaths.ME_CV + "/source-freshness")
    public CvFreshnessResponse getFreshness() {
        return freshnessService.getFreshness();
    }

    @PostMapping(ApiPaths.ME_CV + "/preview")
    @ResponseStatus(HttpStatus.OK)
    public CvPreviewResponse createPreview(@Valid @RequestBody CvPreviewRequest request) {
        return previewService.createPreview(request);
    }

    @GetMapping(ApiPaths.ME_CV)
    public ResponseEntity<CvResponse> getCurrent() {
        CvResponse response = saveService.getCurrent();
        return ResponseEntity.ok()
                .eTag(IfMatchSupport.formatVersion(response.revision()))
                .body(response);
    }

    @PutMapping(ApiPaths.ME_CV)
    public ResponseEntity<CvResponse> save(
            @Valid @RequestBody CvSaveRequest request,
            @RequestHeader(value = HttpHeaders.IF_MATCH, required = false) String ifMatch,
            @RequestHeader(value = HttpHeaders.IF_NONE_MATCH, required = false) String ifNoneMatch) {
        Long revision = ifMatch == null ? null : IfMatchSupport.parseVersion(ifMatch);
        var result = saveService.save(request.previewId(), revision, ifNoneMatch);
        return ResponseEntity.status(result.created() ? HttpStatus.CREATED : HttpStatus.OK)
                .eTag(IfMatchSupport.formatVersion(result.response().revision()))
                .body(result.response());
    }

    @GetMapping(value = ApiPaths.ME_CV + "/download", produces = MediaType.APPLICATION_PDF_VALUE)
    public ResponseEntity<ByteArrayResource> download() {
        var file = downloadService.downloadCurrent();
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + file.fileName() + "\"")
                .header(HttpHeaders.CACHE_CONTROL, "no-store")
                .header("X-Content-Type-Options", "nosniff")
                .contentLength(file.fileSizeBytes())
                .body(new ByteArrayResource(file.bytes()));
    }
}
