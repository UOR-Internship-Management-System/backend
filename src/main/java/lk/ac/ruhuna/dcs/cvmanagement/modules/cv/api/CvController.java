package lk.ac.ruhuna.dcs.cvmanagement.modules.cv.api;

import jakarta.validation.Valid;
import lk.ac.ruhuna.dcs.cvmanagement.modules.cv.api.dto.request.CvPreviewRequest;
import lk.ac.ruhuna.dcs.cvmanagement.modules.cv.api.dto.request.CvSaveRequest;
import lk.ac.ruhuna.dcs.cvmanagement.modules.cv.api.dto.response.CvFreshnessResponse;
import lk.ac.ruhuna.dcs.cvmanagement.modules.cv.api.dto.response.CvPreviewResponse;
import lk.ac.ruhuna.dcs.cvmanagement.modules.cv.api.dto.response.CvResponse;
import lk.ac.ruhuna.dcs.cvmanagement.modules.cv.application.CvFreshnessService;
import lk.ac.ruhuna.dcs.cvmanagement.modules.cv.application.CvPreviewService;
import lk.ac.ruhuna.dcs.cvmanagement.modules.cv.application.CvSaveService;
import lk.ac.ruhuna.dcs.cvmanagement.shared.api.ApiPaths;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
public class CvController {

    private final CvFreshnessService freshnessService;
    private final CvPreviewService previewService;
    private final CvSaveService saveService;

    public CvController(CvFreshnessService freshnessService, CvPreviewService previewService, CvSaveService saveService) {
        this.freshnessService = freshnessService;
        this.previewService = previewService;
        this.saveService = saveService;
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
    public CvResponse getCurrent() {
        return saveService.getCurrent();
    }

    @PutMapping(ApiPaths.ME_CV)
    public CvResponse save(
        @Valid @RequestBody CvSaveRequest request,
        @RequestHeader(value = HttpHeaders.IF_MATCH, required = false) String ifMatch,
        @RequestHeader(value = HttpHeaders.IF_NONE_MATCH, required = false) String ifNoneMatch) {
        Long revision = ifMatch != null ? lk.ac.ruhuna.dcs.cvmanagement.shared.http.IfMatchSupport.parseVersion(ifMatch) : null;
        boolean noneMatchStar = "*".equals(ifNoneMatch);
        return saveService.save(request.previewId(), revision, noneMatchStar);
    }
}
