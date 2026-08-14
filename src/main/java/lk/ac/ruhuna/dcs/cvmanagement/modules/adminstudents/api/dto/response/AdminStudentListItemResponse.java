package lk.ac.ruhuna.dcs.cvmanagement.modules.adminstudents.api.dto.response;

import java.math.BigDecimal;
import java.util.UUID;

/** Read-only registered-Student roster item matching OpenAPI StudentSummaryResponse. */
public record AdminStudentListItemResponse(
        UUID studentId,
        String indexNumber,
        String fullName,
        String universityEmail,
        String degreeProgram,
        String academicBatch,
        int currentLevel,
        BigDecimal officialGpa) {
}
