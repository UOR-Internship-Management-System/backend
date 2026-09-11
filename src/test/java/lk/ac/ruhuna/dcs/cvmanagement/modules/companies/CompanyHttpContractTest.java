package lk.ac.ruhuna.dcs.cvmanagement.modules.companies;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import lk.ac.ruhuna.dcs.cvmanagement.modules.companies.api.CompanyController;
import lk.ac.ruhuna.dcs.cvmanagement.modules.companies.application.CompanyService;
import lk.ac.ruhuna.dcs.cvmanagement.modules.companies.domain.exception.DuplicateCompanyException;
import lk.ac.ruhuna.dcs.cvmanagement.shared.error.GlobalExceptionHandler;
import lk.ac.ruhuna.dcs.cvmanagement.shared.error.ProblemDetailsFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

class CompanyHttpContractTest {

    private CompanyService service;
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        service = mock(CompanyService.class);
        LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();
        validator.afterPropertiesSet();
        mockMvc = MockMvcBuilders
                .standaloneSetup(new CompanyController(service))
                .setValidator(validator)
                .setMessageConverters(new MappingJackson2HttpMessageConverter(new ObjectMapper()))
                .setControllerAdvice(new GlobalExceptionHandler(new ProblemDetailsFactory()))
                .build();
    }

    @Test
    void patchWithoutIfMatchReturns428ProblemDetails() throws Exception {
        mockMvc.perform(patch("/api/v1/admin/companies/00000000-0000-0000-0000-000000000001")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"notes\":\"updated\"}"))
                .andExpect(status().isPreconditionRequired())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.code").value("IF_MATCH_REQUIRED"))
                .andExpect(jsonPath("$.correlationId").isNotEmpty());
    }

    @Test
    void duplicateCreateReturnsStable409Code() throws Exception {
        doThrow(new DuplicateCompanyException()).when(service).create(any());

        mockMvc.perform(post("/api/v1/admin/companies")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Example Technologies\"}"))
                .andExpect(status().isConflict())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.code").value("DUPLICATE_COMPANY"));
    }


    @Test
    void createRejectsRemovedActiveField() throws Exception {
        mockMvc.perform(post("/api/v1/admin/companies")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Example Technologies\",\"active\":true}"))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.code").value("BAD_REQUEST"));
    }

    @Test
    void invalidCreateBodyIsRejectedBeforeServiceInvocation() throws Exception {
        mockMvc.perform(post("/api/v1/admin/companies")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"   \",\"contactEmail\":\"not-an-email\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.code").value("VALIDATION_FAILED"));
    }
}
