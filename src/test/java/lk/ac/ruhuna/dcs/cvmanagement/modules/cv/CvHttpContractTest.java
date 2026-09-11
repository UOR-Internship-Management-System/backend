package lk.ac.ruhuna.dcs.cvmanagement.modules.cv;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
import lk.ac.ruhuna.dcs.cvmanagement.modules.cv.api.CvController;
import lk.ac.ruhuna.dcs.cvmanagement.modules.cv.api.dto.response.CvPreviewConfigurationResponse;
import lk.ac.ruhuna.dcs.cvmanagement.modules.cv.api.dto.response.CvResponse;
import lk.ac.ruhuna.dcs.cvmanagement.modules.cv.api.dto.response.GeneratedFileMetadataResponse;
import lk.ac.ruhuna.dcs.cvmanagement.modules.cv.application.CvDownloadService;
import lk.ac.ruhuna.dcs.cvmanagement.modules.cv.application.CvFreshnessService;
import lk.ac.ruhuna.dcs.cvmanagement.modules.cv.application.CvPreviewService;
import lk.ac.ruhuna.dcs.cvmanagement.modules.cv.application.CvSaveResult;
import lk.ac.ruhuna.dcs.cvmanagement.modules.cv.application.CvSaveService;
import lk.ac.ruhuna.dcs.cvmanagement.modules.cv.application.port.ActiveCvFileResolver;
import lk.ac.ruhuna.dcs.cvmanagement.modules.cv.domain.exception.CvPreconditionRequiredException;
import lk.ac.ruhuna.dcs.cvmanagement.modules.cv.domain.exception.CvPreviewExpiredException;
import lk.ac.ruhuna.dcs.cvmanagement.modules.cv.domain.exception.StaleCvException;
import lk.ac.ruhuna.dcs.cvmanagement.shared.error.GlobalExceptionHandler;
import lk.ac.ruhuna.dcs.cvmanagement.shared.error.ProblemDetailsFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.converter.ResourceHttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

class CvHttpContractTest {

    private CvSaveService saveService;
    private CvDownloadService downloadService;
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        CvFreshnessService freshnessService = mock(CvFreshnessService.class);
        CvPreviewService previewService = mock(CvPreviewService.class);
        saveService = mock(CvSaveService.class);
        downloadService = mock(CvDownloadService.class);

        LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();
        validator.afterPropertiesSet();
        mockMvc = MockMvcBuilders.standaloneSetup(
                        new CvController(freshnessService, previewService, saveService, downloadService))
                .setValidator(validator)
                .setMessageConverters(
                        new MappingJackson2HttpMessageConverter(new ObjectMapper().findAndRegisterModules()),
                        new ResourceHttpMessageConverter())
                .setControllerAdvice(new GlobalExceptionHandler(new ProblemDetailsFactory()))
                .build();
    }

    @Test
    void firstSaveReturns201AndStrongRevisionEtag() throws Exception {
        CvResponse response = response(1);
        when(saveService.save(any(), any(), any())).thenReturn(new CvSaveResult(response, true));

        mockMvc.perform(put("/api/v1/me/cv")
                        .header(HttpHeaders.IF_NONE_MATCH, "*")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"previewId\":\"70000000-0000-4000-8000-000000000001\"}"))
                .andExpect(status().isCreated())
                .andExpect(header().string(HttpHeaders.ETAG, "\"1\""))
                .andExpect(jsonPath("$.revision").value(1))
                .andExpect(jsonPath("$.downloadUrl").value("/me/cv/download"))
                .andExpect(jsonPath("$.pdfFile.fileSizeBytes").value(128));
    }

    @Test
    void missingConditionalHeaderReturnsCvSpecific428ProblemCode() throws Exception {
        when(saveService.save(any(), any(), any())).thenThrow(new CvPreconditionRequiredException());

        mockMvc.perform(put("/api/v1/me/cv")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"previewId\":\"70000000-0000-4000-8000-000000000001\"}"))
                .andExpect(status().isPreconditionRequired())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.code").value("PRECONDITION_REQUIRED"));
    }


    @Test
    void replacementReturns200WithNewRevisionEtag() throws Exception {
        CvResponse response = response(5);
        when(saveService.save(any(), any(), any())).thenReturn(new CvSaveResult(response, false));

        mockMvc.perform(put("/api/v1/me/cv")
                        .header(HttpHeaders.IF_MATCH, "\"4\"")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"previewId\":\"70000000-0000-4000-8000-000000000001\"}"))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.ETAG, "\"5\""))
                .andExpect(jsonPath("$.revision").value(5));
    }

    @Test
    void staleRevisionAndExpiredPreviewUseContractProblemCodes() throws Exception {
        when(saveService.save(any(), any(), any()))
                .thenThrow(new StaleCvException())
                .thenThrow(new CvPreviewExpiredException());

        mockMvc.perform(put("/api/v1/me/cv")
                        .header(HttpHeaders.IF_MATCH, "\"2\"")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"previewId\":\"70000000-0000-4000-8000-000000000001\"}"))
                .andExpect(status().isPreconditionFailed())
                .andExpect(jsonPath("$.code").value("STALE_VERSION"));

        mockMvc.perform(put("/api/v1/me/cv")
                        .header(HttpHeaders.IF_MATCH, "\"2\"")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"previewId\":\"70000000-0000-4000-8000-000000000001\"}"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("CV_PREVIEW_EXPIRED"));
    }

    @Test
    void currentCvReturnsRevisionEtag() throws Exception {
        when(saveService.getCurrent()).thenReturn(response(4));

        mockMvc.perform(get("/api/v1/me/cv"))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.ETAG, "\"4\""))
                .andExpect(jsonPath("$.revision").value(4));
    }

    @Test
    void studentDownloadUsesPrivateNoStoreAndNosniffHeaders() throws Exception {
        byte[] bytes = "%PDF-1.7\n".getBytes(java.nio.charset.StandardCharsets.US_ASCII);
        when(downloadService.downloadCurrent()).thenReturn(new ActiveCvFileResolver.ResolvedCvFile(
                UUID.randomUUID(), 2, "cv-safe.pdf", bytes.length, bytes));

        mockMvc.perform(get("/api/v1/me/cv/download"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_PDF))
                .andExpect(header().string(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"cv-safe.pdf\""))
                .andExpect(header().string(HttpHeaders.CACHE_CONTROL, "private, no-store"))
                .andExpect(header().string("X-Content-Type-Options", "nosniff"))
                .andExpect(content().bytes(bytes));
    }

    private CvResponse response(int revision) {
        OffsetDateTime generatedAt = OffsetDateTime.parse("2026-08-19T04:00:00Z");
        OffsetDateTime savedAt = generatedAt.plusMinutes(1);
        return new CvResponse(
                UUID.fromString("50000000-0000-4000-8000-000000000004"),
                revision,
                generatedAt.minusMinutes(1),
                generatedAt,
                savedAt,
                "/me/cv/download",
                "CURRENT",
                new CvPreviewConfigurationResponse(List.of(), List.of(), List.of(), List.of(), List.of()),
                new GeneratedFileMetadataResponse("cv-safe.pdf", "application/pdf", 128, generatedAt));
    }
}
