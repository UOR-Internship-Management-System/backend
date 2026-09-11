package lk.ac.ruhuna.dcs.cvmanagement.modules.academics.api;

import lk.ac.ruhuna.dcs.cvmanagement.modules.academics.api.dto.response.AcademicRecordResponse;
import lk.ac.ruhuna.dcs.cvmanagement.modules.academics.application.AcademicRecordQueryService;
import lk.ac.ruhuna.dcs.cvmanagement.shared.api.ApiPaths;
import lk.ac.ruhuna.dcs.cvmanagement.shared.pagination.dto.PagedResponse;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** Read-only Admin inspection endpoint for committed official academic records. */
@RestController
@RequestMapping(ApiPaths.ADMIN_ACADEMIC_RECORDS)
public class AdminAcademicRecordsController {
    private final AcademicRecordQueryService queryService;

    public AdminAcademicRecordsController(AcademicRecordQueryService queryService) { this.queryService = queryService; }

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public PagedResponse<AcademicRecordResponse> list(
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size,
            @RequestParam(required = false) String sort,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String courseCode,
            @RequestParam(required = false) String studentId) {
        return queryService.listAdminRecords(page, size, sort, search, courseCode, studentId);
    }
}
