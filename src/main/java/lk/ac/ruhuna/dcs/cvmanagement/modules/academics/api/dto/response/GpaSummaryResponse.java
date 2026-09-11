package lk.ac.ruhuna.dcs.cvmanagement.modules.academics.api.dto.response;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

/** Read-only Computer Science GPA summary for the authenticated Student. */
public record GpaSummaryResponse(
    UUID studentId,
    String status,
    BigDecimal computerScienceGpa,
    BigDecimal totalCredits,
    OffsetDateTime calculatedAt,
    AcademicRecordSourceResponse source) {
}
