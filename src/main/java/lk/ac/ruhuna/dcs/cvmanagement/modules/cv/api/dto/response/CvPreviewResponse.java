package lk.ac.ruhuna.dcs.cvmanagement.modules.cv.api.dto.response;

import java.time.OffsetDateTime;
import java.util.UUID;

public record CvPreviewResponse(
    UUID previewId,
    String htmlPreview,
    CvFreshnessResponse freshness,
    CvPreviewConfigurationResponse configuration,
    OffsetDateTime generatedAt,
    OffsetDateTime expiresAt) {
}
