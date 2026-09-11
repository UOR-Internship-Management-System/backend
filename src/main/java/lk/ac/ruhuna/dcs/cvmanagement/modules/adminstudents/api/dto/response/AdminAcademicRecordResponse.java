package lk.ac.ruhuna.dcs.cvmanagement.modules.adminstudents.api.dto.response;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

/** Read-only committed academic-record projection for one registered Student. */
public record AdminAcademicRecordResponse(
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
