package lk.ac.ruhuna.dcs.cvmanagement.modules.cv.api.dto.response;

import java.time.OffsetDateTime;

public record GeneratedFileMetadataResponse(
    String fileName, String mediaType, long fileSizeBytes, OffsetDateTime generatedAt) {
}
