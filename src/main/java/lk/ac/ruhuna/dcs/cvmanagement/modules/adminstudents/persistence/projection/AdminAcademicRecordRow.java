package lk.ac.ruhuna.dcs.cvmanagement.modules.adminstudents.persistence.projection;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

/** Immutable database projection for one committed official academic record. */
public record AdminAcademicRecordRow(
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
