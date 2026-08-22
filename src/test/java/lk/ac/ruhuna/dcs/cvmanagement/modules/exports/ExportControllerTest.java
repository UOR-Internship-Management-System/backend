package lk.ac.ruhuna.dcs.cvmanagement.modules.exports;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.ByteArrayInputStream;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
import lk.ac.ruhuna.dcs.cvmanagement.modules.exports.api.ExportController;
import lk.ac.ruhuna.dcs.cvmanagement.modules.exports.api.dto.response.ExportFileResponse;
import lk.ac.ruhuna.dcs.cvmanagement.modules.exports.api.dto.response.ExportJobResponse;
import lk.ac.ruhuna.dcs.cvmanagement.modules.exports.application.ExportJobService;
import lk.ac.ruhuna.dcs.cvmanagement.modules.exports.domain.policy.ExportFormat;
import lk.ac.ruhuna.dcs.cvmanagement.modules.exports.domain.policy.ExportStatus;
import lk.ac.ruhuna.dcs.cvmanagement.modules.exports.domain.policy.ExportType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.converter.ResourceHttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

class ExportControllerTest {
    private final UUID shortlistId = UUID.fromString("d2000000-0000-4000-8000-000000000001");
    private final UUID jobId = UUID.fromString("d2000000-0000-4000-8000-000000000002");
    private ExportJobService service;
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        service = mock(ExportJobService.class);
        LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();
        validator.afterPropertiesSet();
        mockMvc = MockMvcBuilders.standaloneSetup(new ExportController(service))
                .setValidator(validator)
                .setMessageConverters(
                        new MappingJackson2HttpMessageConverter(new ObjectMapper().findAndRegisterModules()),
                        new ResourceHttpMessageConverter())
                .build();
    }

    @Test
    void createReturnsAcceptedLocationAndRetryAfter() throws Exception {
        when(service.create(any(), any(), any())).thenReturn(queued());

        mockMvc.perform(post("/api/v1/admin/exports/shortlists/{shortlistId}", shortlistId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"format\":\"CSV\"}"))
                .andExpect(status().isAccepted())
                .andExpect(header().string(HttpHeaders.LOCATION, "/api/v1/admin/exports/" + jobId))
                .andExpect(header().string(HttpHeaders.RETRY_AFTER, "2"))
                .andExpect(jsonPath("$.status").value("QUEUED"));
    }

    @Test
    void completedCsvIsStreamedAsAttachmentWithoutCaching() throws Exception {
        when(service.download(jobId, ExportType.SHORTLIST_SUMMARY_CSV)).thenReturn(
                new ExportFileResponse("shortlist.csv", "text/csv", 3, new ByteArrayInputStream("a,b".getBytes())));

        mockMvc.perform(get("/api/v1/admin/exports/{jobId}/download", jobId))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.CACHE_CONTROL, "no-store"))
                .andExpect(header().string(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"shortlist.csv\""));
    }

    private ExportJobResponse queued() {
        return new ExportJobResponse(
                jobId, shortlistId, ExportType.SHORTLIST_SUMMARY_CSV, ExportFormat.CSV, ExportStatus.QUEUED,
                0, 0, 0, List.of(), List.of(), false, null,
                OffsetDateTime.parse("2026-08-22T03:00:00Z"), null, null, null, null, null);
    }
}
