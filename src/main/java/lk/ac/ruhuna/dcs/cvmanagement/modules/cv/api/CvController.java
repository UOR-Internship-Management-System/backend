package lk.ac.ruhuna.dcs.cvmanagement.modules.cv.api;

import lk.ac.ruhuna.dcs.cvmanagement.modules.cv.api.dto.response.CvFreshnessResponse;
import lk.ac.ruhuna.dcs.cvmanagement.modules.cv.application.CvFreshnessService;
import lk.ac.ruhuna.dcs.cvmanagement.shared.api.ApiPaths;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class CvController {

    private final CvFreshnessService freshnessService;

    public CvController(CvFreshnessService freshnessService) {
        this.freshnessService = freshnessService;
    }

    @GetMapping(ApiPaths.ME_CV + "/source-freshness")
    public CvFreshnessResponse getFreshness() {
        return freshnessService.getFreshness();
    }
}
