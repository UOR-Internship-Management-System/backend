package lk.ac.ruhuna.dcs.cvmanagement.modules.cv.api.dto.response;

import java.time.OffsetDateTime;
import java.util.UUID;

public record CvResponse(
    UUID cvId,
    int revision,
    OffsetDateTime createdAt,
    OffsetDateTime generatedAt,
    OffsetDateTime savedAt,
    String downloadUrl,
    String freshnessStatus,
    CvPreviewConfigurationResponse configuration,
    GeneratedFileMetadataResponse pdfFile) {
}
