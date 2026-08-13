package lk.ac.ruhuna.dcs.cvmanagement.shared.api.dto;

import java.time.OffsetDateTime;
import java.util.UUID;

public record FileAssetResponse(
    UUID fileId, String fileName, String mimeType, long fileSizeBytes, String url, OffsetDateTime createdAt) {
}
