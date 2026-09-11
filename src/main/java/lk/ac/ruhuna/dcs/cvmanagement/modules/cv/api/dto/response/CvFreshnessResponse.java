package lk.ac.ruhuna.dcs.cvmanagement.modules.cv.api.dto.response;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

public record CvFreshnessResponse(
    String status,
    List<String> changedAreas,
    UUID cvId,
    OffsetDateTime savedAt,
    OffsetDateTime evaluatedAt,
    String message) {
}
