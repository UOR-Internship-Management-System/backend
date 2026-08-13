package lk.ac.ruhuna.dcs.cvmanagement.modules.academics.api.dto.response;

import java.time.OffsetDateTime;
import java.util.UUID;

/** Result of a successful all-or-nothing Academic Ledger commit. */
public record AcademicLedgerCommitResponse(
        UUID uploadId,
        String status,
        int committedRecords,
        int affectedStudents,
        int recalculatedGpaCount,
        OffsetDateTime committedAt) {
}
