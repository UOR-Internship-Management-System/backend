package lk.ac.ruhuna.dcs.cvmanagement.modules.academics.api.dto.response;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

/** Read-only projection of one committed official academic result. */
public record AcademicRecordResponse(
        UUID academicRecordId,
        UUID subjectId,
        String courseCode,
        String courseTitle,
        BigDecimal credits,
        String letterGrade,
        BigDecimal gradePoint,
        String semester,
        String academicYear,
        int attemptNumber,
        String resultStatus,
        OffsetDateTime committedAt) {
}
