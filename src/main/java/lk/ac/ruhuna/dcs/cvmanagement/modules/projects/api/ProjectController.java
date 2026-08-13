package lk.ac.ruhuna.dcs.cvmanagement.modules.projects.api;

import jakarta.validation.Valid;
import java.util.UUID;
import lk.ac.ruhuna.dcs.cvmanagement.modules.projects.api.dto.request.ProjectCreateRequest;
import lk.ac.ruhuna.dcs.cvmanagement.modules.projects.api.dto.request.ProjectUpdateRequest;
import lk.ac.ruhuna.dcs.cvmanagement.modules.projects.api.dto.response.ProjectResponse;
import lk.ac.ruhuna.dcs.cvmanagement.modules.projects.application.ProjectService;
import lk.ac.ruhuna.dcs.cvmanagement.shared.api.ApiPaths;
import lk.ac.ruhuna.dcs.cvmanagement.shared.http.IfMatchSupport;
import lk.ac.ruhuna.dcs.cvmanagement.shared.pagination.dto.PagedResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(ApiPaths.ME_PROJECTS)
public class ProjectController {

    private final ProjectService service;

    public ProjectController(ProjectService service) {
        this.service = service;
    }

    @GetMapping
    public PagedResponse<ProjectResponse> list(
        @RequestParam(required = false) String search,
        @RequestParam(required = false) Integer page,
        @RequestParam(required = false) Integer size,
        @RequestParam(required = false) String sort) {
        return service.list(search, page, size, sort);
    }

    @GetMapping("/{projectId}")
    public ProjectResponse get(@PathVariable UUID projectId) {
        return service.get(projectId);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ProjectResponse create(@Valid @RequestBody ProjectCreateRequest request) {
        return service.create(request);
    }

    @PatchMapping("/{projectId}")
    public ProjectResponse update(
        @PathVariable UUID projectId,
        @Valid @RequestBody ProjectUpdateRequest request,
        @RequestHeader(HttpHeaders.IF_MATCH) String ifMatch) {
        return service.update(projectId, request, IfMatchSupport.parseVersion(ifMatch));
    }

    @DeleteMapping("/{projectId}")
    public ResponseEntity<Void> delete(
        @PathVariable UUID projectId,
        @RequestHeader(HttpHeaders.IF_MATCH) String ifMatch) {
        service.delete(projectId, IfMatchSupport.parseVersion(ifMatch));
        return ResponseEntity.noContent().build();
    }
}
