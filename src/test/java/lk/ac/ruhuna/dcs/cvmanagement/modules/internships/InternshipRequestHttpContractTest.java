package lk.ac.ruhuna.dcs.cvmanagement.modules.internships;

import static org.mockito.Mockito.mock;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import lk.ac.ruhuna.dcs.cvmanagement.modules.internships.api.InternshipRequestController;
import lk.ac.ruhuna.dcs.cvmanagement.modules.internships.application.InternshipRequestService;
import lk.ac.ruhuna.dcs.cvmanagement.shared.error.GlobalExceptionHandler;
import lk.ac.ruhuna.dcs.cvmanagement.shared.error.ProblemDetailsFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

class InternshipRequestHttpContractTest {

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();
        validator.afterPropertiesSet();
        mockMvc = MockMvcBuilders.standaloneSetup(new InternshipRequestController(mock(InternshipRequestService.class)))
                .setValidator(validator)
                .setControllerAdvice(new GlobalExceptionHandler(new ProblemDetailsFactory()))
                .build();
    }

    @Test
    void patchWithoutIfMatchReturns428() throws Exception {
        mockMvc.perform(patch("/api/v1/admin/internship-requests/00000000-0000-0000-0000-000000000001")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"Updated\"}"))
                .andExpect(status().isPreconditionRequired())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.code").value("IF_MATCH_REQUIRED"));
    }

    @Test
    void createRejectsMissingCompanyAndTitleBeforeService() throws Exception {
        mockMvc.perform(post("/api/v1/admin/internship-requests")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"   \",\"requiredSkills\":[]}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_FAILED"));
    }
}
