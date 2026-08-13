package lk.ac.ruhuna.dcs.cvmanagement.modules.academics.application;

import com.fasterxml.jackson.databind.JsonNode;
import java.math.BigDecimal;

/** Normalized transport row ready for staging; authoritative domain fields remain unresolved. */
record AcademicLedgerParsedRow(
        int rowNumber,
        JsonNode rawPayload,
        String studentIndexNumber,
        String courseCode,
        BigDecimal credits,
        String letterGrade,
        String semester,
        String academicYear,
        short attemptNumber,
        String resultStatus) {
}
