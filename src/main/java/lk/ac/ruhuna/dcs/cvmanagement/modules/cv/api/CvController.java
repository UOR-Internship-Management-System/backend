package lk.ac.ruhuna.dcs.cvmanagement.modules.cv.api;

import lk.ac.ruhuna.dcs.cvmanagement.modules.cv.api.dto.request.CvPreviewRequest;
import lk.ac.ruhuna.dcs.cvmanagement.modules.cv.api.dto.response.CvFreshnessResponse;
import lk.ac.ruhuna.dcs.cvmanagement.modules.cv.api.dto.response.CvPreviewResponse;
import lk.ac.ruhuna.dcs.cvmanagement.modules.cv.application.CvFreshnessService;
import lk.ac.ruhuna.dcs.cvmanagement.modules.cv.application.CvPreviewService;
import lk.ac.ruhuna.dcs.cvmanagement.shared.api.ApiPaths;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class CvController {

    private final CvFreshnessService freshnessService;
    private final CvPreviewService previewService;

    public CvController(CvFreshnessService freshnessService, CvPreviewService previewService) {
        this.freshnessService = freshnessService;
        this.previewService = previewService;
    }

    @GetMapping(ApiPaths.ME_CV + "/source-freshness")
    public CvFreshnessResponse getFreshness() {
        return freshnessService.getFreshness();
    }

    @PostMapping(ApiPaths.ME_CV + "/preview")
    @ResponseStatus(HttpStatus.OK)
    public CvPreviewResponse createPreview(@RequestBody CvPreviewRequest request) {
        return previewService.createPreview(request);
    }
}
