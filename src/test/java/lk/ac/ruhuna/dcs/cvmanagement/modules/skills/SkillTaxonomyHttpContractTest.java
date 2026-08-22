package lk.ac.ruhuna.dcs.cvmanagement.modules.skills;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import java.util.UUID;
import lk.ac.ruhuna.dcs.cvmanagement.modules.skills.api.SkillTaxonomyController;
import lk.ac.ruhuna.dcs.cvmanagement.modules.skills.api.dto.response.SkillCategoryResponse;
import lk.ac.ruhuna.dcs.cvmanagement.modules.skills.api.dto.response.SkillClusterResponse;
import lk.ac.ruhuna.dcs.cvmanagement.modules.skills.application.SkillTaxonomyService;
import lk.ac.ruhuna.dcs.cvmanagement.shared.pagination.dto.PageMetadata;
import lk.ac.ruhuna.dcs.cvmanagement.shared.pagination.dto.PagedResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

class SkillTaxonomyHttpContractTest {

    private SkillTaxonomyService service;
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        service = mock(SkillTaxonomyService.class);
        mockMvc = MockMvcBuilders
                .standaloneSetup(new SkillTaxonomyController(service))
                .setMessageConverters(new MappingJackson2HttpMessageConverter(new ObjectMapper()))
                .build();
    }

    @Test
    void pagedClustersOmitUnavailableNestedCategories() throws Exception {
        UUID clusterId = UUID.fromString("00000000-0000-0000-0000-000000000101");
        PagedResponse<SkillClusterResponse> response = new PagedResponse<>(
                List.of(new SkillClusterResponse(clusterId, "Software Engineering", null, null)),
                new PageMetadata(0, 100, 1, 1, "name,asc"));
        when(service.listClusters(0, 100, "name,asc")).thenReturn(response);

        mockMvc.perform(get("/api/v1/skill-taxonomy/clusters")
                        .param("page", "0")
                        .param("size", "100")
                        .param("sort", "name,asc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items[0].clusterId").value(clusterId.toString()))
                .andExpect(jsonPath("$.items[0].name").value("Software Engineering"))
                .andExpect(jsonPath("$.items[0].categories").doesNotExist())
                .andExpect(jsonPath("$.page.totalElements").value(1));
    }

    @Test
    void pagedCategoriesOmitUnavailableNestedSkills() throws Exception {
        UUID categoryId = UUID.fromString("00000000-0000-0000-0000-000000000201");
        PagedResponse<SkillCategoryResponse> response = new PagedResponse<>(
                List.of(new SkillCategoryResponse(categoryId, "Backend Development", null, null)),
                new PageMetadata(0, 100, 1, 1, "name,asc"));
        when(service.listCategories(null, 0, 100, "name,asc")).thenReturn(response);

        mockMvc.perform(get("/api/v1/skill-taxonomy/categories")
                        .param("page", "0")
                        .param("size", "100")
                        .param("sort", "name,asc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items[0].categoryId").value(categoryId.toString()))
                .andExpect(jsonPath("$.items[0].name").value("Backend Development"))
                .andExpect(jsonPath("$.items[0].skills").doesNotExist())
                .andExpect(jsonPath("$.page.totalElements").value(1));
    }
}
