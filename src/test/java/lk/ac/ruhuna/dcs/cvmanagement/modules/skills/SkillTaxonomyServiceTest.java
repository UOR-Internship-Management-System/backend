package lk.ac.ruhuna.dcs.cvmanagement.modules.skills;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import lk.ac.ruhuna.dcs.cvmanagement.modules.skills.application.SkillTaxonomyService;
import lk.ac.ruhuna.dcs.cvmanagement.modules.skills.mapper.SkillMapper;
import lk.ac.ruhuna.dcs.cvmanagement.modules.skills.persistence.repository.SkillCategoryRepository;
import lk.ac.ruhuna.dcs.cvmanagement.modules.skills.persistence.repository.SkillCoreClusterRepository;
import lk.ac.ruhuna.dcs.cvmanagement.modules.skills.persistence.repository.SkillRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

class SkillTaxonomyServiceTest {

    private final SkillCoreClusterRepository clusterRepository = mock(SkillCoreClusterRepository.class);
    private final SkillCategoryRepository categoryRepository = mock(SkillCategoryRepository.class);
    private final SkillRepository skillRepository = mock(SkillRepository.class);
    private final SkillMapper mapper = mock(SkillMapper.class);

    private SkillTaxonomyService service;

    @BeforeEach
    void setUp() {
        service = new SkillTaxonomyService(clusterRepository, categoryRepository, skillRepository, mapper);
    }

    @Test
    void mapsPublicNameSortToClusterName() {
        when(clusterRepository.findByActiveTrue(any(Pageable.class)))
            .thenAnswer(invocation -> Page.empty(invocation.getArgument(0)));

        service.listClusters(0, 100, "name,asc");

        ArgumentCaptor<Pageable> pageable = ArgumentCaptor.forClass(Pageable.class);
        verify(clusterRepository).findByActiveTrue(pageable.capture());
        assertThat(pageable.getValue().getSort().getOrderFor("clusterName"))
            .isNotNull()
            .extracting(Sort.Order::getDirection)
            .isEqualTo(Sort.Direction.ASC);
        assertThat(pageable.getValue().getSort().getOrderFor("name")).isNull();
    }

    @Test
    void mapsPublicNameSortToCategoryName() {
        when(categoryRepository.search(isNull(), any(Pageable.class)))
            .thenAnswer(invocation -> Page.empty(invocation.getArgument(1)));

        service.listCategories(null, 0, 100, "name,asc");

        ArgumentCaptor<Pageable> pageable = ArgumentCaptor.forClass(Pageable.class);
        verify(categoryRepository).search(isNull(), pageable.capture());
        assertThat(pageable.getValue().getSort().getOrderFor("categoryName"))
            .isNotNull()
            .extracting(Sort.Order::getDirection)
            .isEqualTo(Sort.Direction.ASC);
        assertThat(pageable.getValue().getSort().getOrderFor("name")).isNull();
    }
}
