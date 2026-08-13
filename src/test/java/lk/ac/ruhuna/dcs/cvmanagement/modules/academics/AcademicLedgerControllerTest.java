package lk.ac.ruhuna.dcs.cvmanagement.modules.academics;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.time.OffsetDateTime;
import java.util.UUID;
import lk.ac.ruhuna.dcs.cvmanagement.modules.academics.api.AcademicLedgerController;
import lk.ac.ruhuna.dcs.cvmanagement.modules.academics.api.dto.response.AcademicLedgerUploadDetailResponse;
import lk.ac.ruhuna.dcs.cvmanagement.modules.academics.application.AcademicLedgerUploadService;
import lk.ac.ruhuna.dcs.cvmanagement.modules.academics.config.AcademicLedgerProperties;
import lk.ac.ruhuna.dcs.cvmanagement.modules.academics.domain.AcademicLedgerUploadStatus;
import lk.ac.ruhuna.dcs.cvmanagement.modules.academics.domain.AcademicLedgerValidationStatus;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockMultipartFile;

class AcademicLedgerControllerTest {

    @Test
    void acceptedUploadReturnsCanonicalPollingHeadersAndReceivedState() {
        AcademicLedgerUploadService service = mock(AcademicLedgerUploadService.class);
        AcademicLedgerProperties properties = new AcademicLedgerProperties(5_242_880L, 2);
        AcademicLedgerController controller = new AcademicLedgerController(service, properties);
        UUID uploadId = UUID.randomUUID();
        MockMultipartFile file = new MockMultipartFile(
                "file", "ledger.csv", "text/csv", "data".getBytes());
        var detail = new AcademicLedgerUploadDetailResponse(
                uploadId,
                "ledger.csv",
                "text/csv",
                4,
                AcademicLedgerUploadStatus.RECEIVED,
                AcademicLedgerValidationStatus.NOT_STARTED,
                0,
                0,
                0,
                OffsetDateTime.parse("2026-08-13T00:00:00Z"),
                null,
                null,
                "The CSV file was accepted and is waiting for processing.",
                2);
        when(service.upload(file)).thenReturn(detail);

        var response = controller.upload(file);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.ACCEPTED);
        assertThat(response.getHeaders().getLocation())
                .hasToString("/api/v1/admin/academic-ledger/uploads/" + uploadId);
        assertThat(response.getHeaders().getFirst("Retry-After")).isEqualTo("2");
        assertThat(response.getBody()).isEqualTo(detail);
    }
}
