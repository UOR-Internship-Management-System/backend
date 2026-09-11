package lk.ac.ruhuna.dcs.cvmanagement.modules.academics;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
import lk.ac.ruhuna.dcs.cvmanagement.modules.academics.api.AdminAcademicRecordsController;
import lk.ac.ruhuna.dcs.cvmanagement.modules.academics.api.dto.response.AcademicRecordResponse;
import lk.ac.ruhuna.dcs.cvmanagement.modules.academics.application.AcademicRecordQueryService;
import lk.ac.ruhuna.dcs.cvmanagement.shared.pagination.dto.PageMetadata;
import lk.ac.ruhuna.dcs.cvmanagement.shared.pagination.dto.PagedResponse;
import org.junit.jupiter.api.Test;

class AdminAcademicRecordsControllerTest {
    @Test
    void returnsTheExactTypedCommittedRecordPageFromTheService() {
        AcademicRecordQueryService service = mock(AcademicRecordQueryService.class);
        AdminAcademicRecordsController controller = new AdminAcademicRecordsController(service);
        var item = new AcademicRecordResponse(
                UUID.randomUUID(), UUID.randomUUID(), "CSC2113", "Data Communication and Computer Networks",
                new BigDecimal("3.0"), "A-", new BigDecimal("3.70"), "Semester 1", "2025/2026",
                1, "PASSED", OffsetDateTime.parse("2026-08-13T02:00:00Z"));
        var expected = new PagedResponse<>(List.of(item), new PageMetadata(0, 20, 1, 1, "academicYear,desc"));
        when(service.listAdminRecords(0, 20, null, "network", null, null)).thenReturn(expected);

        assertThat(controller.list(0, 20, null, "network", null, null)).isEqualTo(expected);
    }
}
