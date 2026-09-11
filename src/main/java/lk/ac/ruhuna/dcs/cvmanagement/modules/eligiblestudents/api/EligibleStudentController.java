package lk.ac.ruhuna.dcs.cvmanagement.modules.eligiblestudents.api;

import jakarta.validation.Valid;
import java.util.UUID;
import lk.ac.ruhuna.dcs.cvmanagement.modules.eligiblestudents.api.dto.request.EligibleStudentRequest;
import lk.ac.ruhuna.dcs.cvmanagement.modules.eligiblestudents.api.dto.response.EligibleStudentImportResponse;
import lk.ac.ruhuna.dcs.cvmanagement.modules.eligiblestudents.api.dto.response.EligibleStudentResponse;
import lk.ac.ruhuna.dcs.cvmanagement.modules.eligiblestudents.application.EligibleStudentService;
import lk.ac.ruhuna.dcs.cvmanagement.shared.api.ApiPaths;
import lk.ac.ruhuna.dcs.cvmanagement.shared.pagination.dto.PagedResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/** Admin-only management of the pre-approved eligible-student roster used to gate registration. */
@RestController
@RequestMapping(ApiPaths.ADMIN_ELIGIBLE_STUDENTS)
@Validated
public class EligibleStudentController {

    private final EligibleStudentService service;

    public EligibleStudentController(EligibleStudentService service) {
        this.service = service;
    }

    @GetMapping
    public PagedResponse<EligibleStudentResponse> list(
        @RequestParam(required = false) String search,
        @RequestParam(required = false) Integer page,
        @RequestParam(required = false) Integer size,
        @RequestParam(required = false) String sort) {
        return service.list(search, page, size, sort);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public EligibleStudentResponse create(@Valid @RequestBody EligibleStudentRequest request) {
        return service.create(request);
    }

    @PatchMapping("/{eligibleStudentId}")
    public EligibleStudentResponse update(
        @PathVariable UUID eligibleStudentId, @Valid @RequestBody EligibleStudentRequest request) {
        return service.update(eligibleStudentId, request);
    }

    @DeleteMapping("/{eligibleStudentId}")
    public ResponseEntity<Void> delete(@PathVariable UUID eligibleStudentId) {
        service.delete(eligibleStudentId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping(path = "/import", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public EligibleStudentImportResponse importFile(
        @RequestPart(value = "file", required = false) MultipartFile file) {
        return service.importFile(file);
    }
}
