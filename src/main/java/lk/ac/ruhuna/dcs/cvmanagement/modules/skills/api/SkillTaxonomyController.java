package lk.ac.ruhuna.dcs.cvmanagement.modules.skills.api;

import java.util.UUID;
import lk.ac.ruhuna.dcs.cvmanagement.modules.skills.api.dto.response.IndividualSkillResponse;
import lk.ac.ruhuna.dcs.cvmanagement.modules.skills.api.dto.response.SkillCategoryResponse;
import lk.ac.ruhuna.dcs.cvmanagement.modules.skills.api.dto.response.SkillClusterResponse;
import lk.ac.ruhuna.dcs.cvmanagement.modules.skills.api.dto.response.SkillTaxonomyResponse;
import lk.ac.ruhuna.dcs.cvmanagement.modules.skills.application.SkillTaxonomyService;
import lk.ac.ruhuna.dcs.cvmanagement.shared.api.ApiPaths;
import lk.ac.ruhuna.dcs.cvmanagement.shared.pagination.dto.PagedResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

// No explicit security matcher needed: /skill-taxonomy/** isn't in the /me/** or /admin/**
// patterns in SecurityConfig, so it falls through to anyRequest().authenticated() — which is
// exactly right, since both STUDENT and ADMIN are allowed to read this per the OpenAPI spec.
@RestController
@RequestMapping(ApiPaths.SKILL_TAXONOMY)
public class SkillTaxonomyController {

    private final SkillTaxonomyService service;

    public SkillTaxonomyController(SkillTaxonomyService service) {
        this.service = service;
    }

    @GetMapping
    public SkillTaxonomyResponse getTree() {
        return service.getTree();
    }

    @GetMapping("/clusters")
    public PagedResponse<SkillClusterResponse> listClusters(
        @RequestParam(required = false) Integer page,
        @RequestParam(required = false) Integer size,
        @RequestParam(required = false) String sort,
        @RequestParam(required = false) String search) {
        return service.listClusters(page, size, sort);
    }

    @GetMapping("/categories")
    public PagedResponse<SkillCategoryResponse> listCategories(
        @RequestParam(required = false) UUID clusterId,
        @RequestParam(required = false) Integer page,
        @RequestParam(required = false) Integer size,
        @RequestParam(required = false) String sort,
        @RequestParam(required = false) String search) {
        return service.listCategories(clusterId, page, size, sort);
    }

    @GetMapping("/skills")
    public PagedResponse<IndividualSkillResponse> listSkills(
        @RequestParam(required = false) UUID clusterId,
        @RequestParam(required = false) UUID categoryId,
        @RequestParam(required = false) String search,
        @RequestParam(required = false) Integer page,
        @RequestParam(required = false) Integer size,
        @RequestParam(required = false) String sort) {
        return service.listSkills(clusterId, categoryId, search, page, size, sort);
    }
}
