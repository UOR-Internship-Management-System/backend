package lk.ac.ruhuna.dcs.cvmanagement.modules.adminstudents.api;

import java.util.UUID;
import lk.ac.ruhuna.dcs.cvmanagement.modules.adminstudents.api.dto.request.AdminAcademicRecordCriteria;
import lk.ac.ruhuna.dcs.cvmanagement.modules.adminstudents.api.dto.request.AdminStudentCollectionCriteria;
import lk.ac.ruhuna.dcs.cvmanagement.modules.adminstudents.api.dto.request.AdminStudentSearchCriteria;
import lk.ac.ruhuna.dcs.cvmanagement.modules.adminstudents.api.dto.response.AdminAcademicRecordResponse;
import lk.ac.ruhuna.dcs.cvmanagement.modules.adminstudents.api.dto.response.AdminDeclaredSkillResponse;
import lk.ac.ruhuna.dcs.cvmanagement.modules.adminstudents.api.dto.response.AdminProjectResponse;
import lk.ac.ruhuna.dcs.cvmanagement.modules.adminstudents.api.dto.response.AdminStudentDetailResponse;
import lk.ac.ruhuna.dcs.cvmanagement.modules.adminstudents.api.dto.response.AdminStudentListItemResponse;
import lk.ac.ruhuna.dcs.cvmanagement.modules.adminstudents.application.AdminStudentInspectionService;
import lk.ac.ruhuna.dcs.cvmanagement.modules.adminstudents.application.RegisteredStudentQueryService;
import lk.ac.ruhuna.dcs.cvmanagement.shared.api.ApiPaths;
import lk.ac.ruhuna.dcs.cvmanagement.shared.pagination.dto.PagedResponse;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** Admin-only, read-only Student inspection endpoints. */
@RestController
@RequestMapping(ApiPaths.ADMIN_STUDENTS)
public class AdminStudentController {

    private final RegisteredStudentQueryService registeredStudentQueryService;
    private final AdminStudentInspectionService adminStudentInspectionService;

    public AdminStudentController(
            RegisteredStudentQueryService registeredStudentQueryService,
            AdminStudentInspectionService adminStudentInspectionService) {
        this.registeredStudentQueryService = registeredStudentQueryService;
        this.adminStudentInspectionService = adminStudentInspectionService;
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

    /** Returns one registered Student's read-only profile and CV-supporting deep-dive summary. */
    @GetMapping(value = "/{studentId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public AdminStudentDetailResponse getStudentDetail(@PathVariable UUID studentId) {
        return adminStudentInspectionService.getDetail(studentId);
    }

    /** Returns the selected Student's declared taxonomy skills without verification state. */
    @GetMapping(value = "/{studentId}/declared-skills", produces = MediaType.APPLICATION_JSON_VALUE)
    public PagedResponse<AdminDeclaredSkillResponse> getDeclaredSkills(
            @PathVariable UUID studentId,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size,
            @RequestParam(required = false) String search) {
        return adminStudentInspectionService.getDeclaredSkills(
                studentId,
                new AdminStudentCollectionCriteria(page, size, search));
    }

    /** Returns Student-owned portfolio projects and canonical project skills read-only. */
    @GetMapping(value = "/{studentId}/projects", produces = MediaType.APPLICATION_JSON_VALUE)
    public PagedResponse<AdminProjectResponse> getProjects(
            @PathVariable UUID studentId,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size,
            @RequestParam(required = false) String search) {
        return adminStudentInspectionService.getProjects(
                studentId,
                new AdminStudentCollectionCriteria(page, size, search));
    }

    /** Returns only committed official academic records for the selected registered Student. */
    @GetMapping(value = "/{studentId}/academic-records", produces = MediaType.APPLICATION_JSON_VALUE)
    public PagedResponse<AdminAcademicRecordResponse> getAcademicRecords(
            @PathVariable UUID studentId,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size,
            @RequestParam(required = false) String sort,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String courseCode) {
        return adminStudentInspectionService.getAcademicRecords(
                studentId,
                new AdminAcademicRecordCriteria(page, size, sort, search, courseCode));
    }
}
