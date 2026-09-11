package lk.ac.ruhuna.dcs.cvmanagement.modules.academics.api.dto.response;

import java.time.OffsetDateTime;
import java.util.UUID;

/** Reference to the committed Academic Ledger upload batch a GPA summary was calculated from. */
public record AcademicRecordSourceResponse(UUID sourceUploadId, OffsetDateTime committedAt) {
}
