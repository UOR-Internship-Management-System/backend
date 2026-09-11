package lk.ac.ruhuna.dcs.cvmanagement.modules.skills.api;

import jakarta.validation.Valid;
import java.util.UUID;
import lk.ac.ruhuna.dcs.cvmanagement.modules.skills.api.dto.request.DeclaredSkillCreateRequest;
import lk.ac.ruhuna.dcs.cvmanagement.modules.skills.api.dto.request.DeclaredSkillUpdateRequest;
import lk.ac.ruhuna.dcs.cvmanagement.modules.skills.api.dto.response.DeclaredSkillResponse;
import lk.ac.ruhuna.dcs.cvmanagement.modules.skills.application.DeclaredSkillService;
import lk.ac.ruhuna.dcs.cvmanagement.shared.api.ApiPaths;
import lk.ac.ruhuna.dcs.cvmanagement.shared.http.IfMatchSupport;
import lk.ac.ruhuna.dcs.cvmanagement.shared.pagination.dto.PagedResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(ApiPaths.ME_DECLARED_SKILLS)
public class DeclaredSkillController {

    private final DeclaredSkillService service;

    public DeclaredSkillController(DeclaredSkillService service) {
        this.service = service;
    }

    @GetMapping
    public PagedResponse<DeclaredSkillResponse> list(
        @RequestParam(required = false) String search,
        @RequestParam(required = false) Integer page,
        @RequestParam(required = false) Integer size,
        @RequestParam(required = false) String sort) {
        return service.list(search, page, size, sort);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public DeclaredSkillResponse create(@Valid @RequestBody DeclaredSkillCreateRequest request) {
        return service.create(request);
    }

    @PatchMapping("/{declaredSkillId}")
    public DeclaredSkillResponse update(
        @PathVariable UUID declaredSkillId,
        @Valid @RequestBody DeclaredSkillUpdateRequest request,
        @RequestHeader(HttpHeaders.IF_MATCH) String ifMatch) {
        long version = IfMatchSupport.parseVersion(ifMatch);
        return service.update(declaredSkillId, request, version);
    }

    @DeleteMapping("/{declaredSkillId}")
    public ResponseEntity<Void> delete(
        @PathVariable UUID declaredSkillId,
        @RequestHeader(HttpHeaders.IF_MATCH) String ifMatch) {
        long version = IfMatchSupport.parseVersion(ifMatch);
        service.delete(declaredSkillId, version);
        return ResponseEntity.noContent().build();
    }
}
