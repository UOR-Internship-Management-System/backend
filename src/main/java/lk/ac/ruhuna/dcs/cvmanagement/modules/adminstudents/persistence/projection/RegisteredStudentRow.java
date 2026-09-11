package lk.ac.ruhuna.dcs.cvmanagement.modules.adminstudents.persistence.projection;

import java.math.BigDecimal;
import java.util.UUID;

/** Immutable database projection for one registered Student roster row. */
public record RegisteredStudentRow(
        UUID studentId,
        String indexNumber,
        String fullName,
        String universityEmail,
        String academicBatch,
        int currentLevel,
        BigDecimal officialGpa) {
}
