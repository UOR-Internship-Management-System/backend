package lk.ac.ruhuna.dcs.cvmanagement.modules.adminstudents.api;

import lk.ac.ruhuna.dcs.cvmanagement.modules.adminstudents.api.dto.request.AdminStudentSearchCriteria;
import lk.ac.ruhuna.dcs.cvmanagement.modules.adminstudents.api.dto.response.AdminStudentListItemResponse;
import lk.ac.ruhuna.dcs.cvmanagement.modules.adminstudents.application.RegisteredStudentQueryService;
import lk.ac.ruhuna.dcs.cvmanagement.shared.api.ApiPaths;
import lk.ac.ruhuna.dcs.cvmanagement.shared.pagination.dto.PagedResponse;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** Admin-only, read-only Student inspection endpoints. */
@RestController
@RequestMapping(ApiPaths.ADMIN_STUDENTS)
public class AdminStudentController {

    private final RegisteredStudentQueryService registeredStudentQueryService;

    public AdminStudentController(RegisteredStudentQueryService registeredStudentQueryService) {
        this.registeredStudentQueryService = registeredStudentQueryService;
    }

    /** Search, filter, sort, and paginate the live registered-Student roster. */
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public PagedResponse<AdminStudentListItemResponse> listRegisteredStudents(
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size,
            @RequestParam(required = false) String sort,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Integer level) {
        return registeredStudentQueryService.list(new AdminStudentSearchCriteria(page, size, sort, search, level));
    }
}
