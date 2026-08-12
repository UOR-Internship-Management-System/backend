package lk.ac.ruhuna.dcs.cvmanagement.modules.skills.application;

import java.util.List;
import java.util.UUID;
import lk.ac.ruhuna.dcs.cvmanagement.modules.skills.api.dto.response.IndividualSkillResponse;
import lk.ac.ruhuna.dcs.cvmanagement.modules.skills.api.dto.response.SkillCategoryResponse;
import lk.ac.ruhuna.dcs.cvmanagement.modules.skills.api.dto.response.SkillClusterResponse;
import lk.ac.ruhuna.dcs.cvmanagement.modules.skills.api.dto.response.SkillTaxonomyResponse;
import lk.ac.ruhuna.dcs.cvmanagement.modules.skills.mapper.SkillMapper;
import lk.ac.ruhuna.dcs.cvmanagement.modules.skills.persistence.entity.SkillCategoryEntity;
import lk.ac.ruhuna.dcs.cvmanagement.modules.skills.persistence.entity.SkillCoreClusterEntity;
import lk.ac.ruhuna.dcs.cvmanagement.modules.skills.persistence.entity.SkillEntity;
import lk.ac.ruhuna.dcs.cvmanagement.modules.skills.persistence.repository.SkillCategoryRepository;
import lk.ac.ruhuna.dcs.cvmanagement.modules.skills.persistence.repository.SkillCoreClusterRepository;
import lk.ac.ruhuna.dcs.cvmanagement.modules.skills.persistence.repository.SkillRepository;
import lk.ac.ruhuna.dcs.cvmanagement.shared.pagination.PageRequestFactory;
import lk.ac.ruhuna.dcs.cvmanagement.shared.pagination.dto.PagedResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class SkillTaxonomyService {

    private final SkillCoreClusterRepository clusterRepository;
    private final SkillCategoryRepository categoryRepository;
    private final SkillRepository skillRepository;
    private final SkillMapper mapper;

    public SkillTaxonomyService(
        SkillCoreClusterRepository clusterRepository,
        SkillCategoryRepository categoryRepository,
        SkillRepository skillRepository,
        SkillMapper mapper) {
        this.clusterRepository = clusterRepository;
        this.categoryRepository = categoryRepository;
        this.skillRepository = skillRepository;
        this.mapper = mapper;
    }

    /** Full nested tree for the Skills page's cascading dropdowns / browse cards. */
    public SkillTaxonomyResponse getTree() {
        List<SkillClusterResponse> clusters = clusterRepository.findByActiveTrueOrderByDisplayOrderAsc().stream()
            .map(this::toClusterResponse)
            .toList();
        return new SkillTaxonomyResponse(clusters);
    }

    private SkillClusterResponse toClusterResponse(SkillCoreClusterEntity cluster) {
        List<SkillCategoryResponse> categories = categoryRepository
            .findByCoreClusterIdOrderByDisplayOrderAsc(cluster.getId()).stream()
            .map(this::toCategoryResponse)
            .toList();
        return new SkillClusterResponse(cluster.getId(), cluster.getClusterName(), cluster.getDescription(), categories);
    }

    private SkillCategoryResponse toCategoryResponse(SkillCategoryEntity category) {
        List<IndividualSkillResponse> skills = skillRepository.findAllForCategory(category.getId()).stream()
            .map(mapper::toResponse)
            .toList();
        return new SkillCategoryResponse(category.getId(), category.getCategoryName(), category.getDescription(), skills);
    }

    public PagedResponse<SkillClusterResponse> listClusters(Integer page, Integer size, String sort) {
        Pageable pageable = PageRequestFactory.build(page, size, sort);
        Page<SkillCoreClusterEntity> result = clusterRepository.findByActiveTrue(pageable);
        return PagedResponse.of(
            result.map(c -> new SkillClusterResponse(c.getId(), c.getClusterName(), c.getDescription(), null)),
            PageRequestFactory.describeSort(sort));
    }

    public PagedResponse<SkillCategoryResponse> listCategories(UUID clusterId, Integer page, Integer size, String sort) {
        Pageable pageable = PageRequestFactory.build(page, size, sort);
        Page<SkillCategoryEntity> result = categoryRepository.search(clusterId, pageable);
        return PagedResponse.of(
            result.map(c -> new SkillCategoryResponse(c.getId(), c.getCategoryName(), c.getDescription(), null)),
            PageRequestFactory.describeSort(sort));
    }

    public PagedResponse<IndividualSkillResponse> listSkills(
        UUID clusterId, UUID categoryId, String search, Integer page, Integer size, String sort) {
        Pageable pageable = PageRequestFactory.build(page, size, sort);
        String searchPattern = "%" + (search == null ? "" : search.toLowerCase()) + "%";   // <-- add this line
        Page<SkillEntity> result = skillRepository.search(clusterId, categoryId, searchPattern, pageable); // <-- change search -> searchPattern here
        return PagedResponse.of(result.map(mapper::toResponse), PageRequestFactory.describeSort(sort));
    }
}
