package lk.ac.ruhuna.dcs.cvmanagement.modules.academics.api;

import lk.ac.ruhuna.dcs.cvmanagement.modules.academics.api.dto.response.AcademicRecordResponse;
import lk.ac.ruhuna.dcs.cvmanagement.modules.academics.api.dto.response.GpaSummaryResponse;
import lk.ac.ruhuna.dcs.cvmanagement.modules.academics.application.StudentAcademicRecordService;
import lk.ac.ruhuna.dcs.cvmanagement.shared.api.ApiPaths;
import lk.ac.ruhuna.dcs.cvmanagement.shared.pagination.dto.PagedResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** Read-only committed academic-record and GPA endpoints for the authenticated Student. */
@RestController
public class StudentAcademicRecordsController {

    private final StudentAcademicRecordService service;

    public StudentAcademicRecordsController(StudentAcademicRecordService service) {
        this.service = service;
    }

    @GetMapping(ApiPaths.ME_ACADEMIC_RECORDS)
    public PagedResponse<AcademicRecordResponse> list(
        @RequestParam(required = false) Integer page,
        @RequestParam(required = false) Integer size,
        @RequestParam(required = false) String sort,
        @RequestParam(required = false) String search) {
        return service.list(page, size, sort, search);
    }

    @GetMapping(ApiPaths.ME_ACADEMIC_RECORDS_GPA)
    public GpaSummaryResponse getGpa() {
        return service.getGpa();
    }
}
