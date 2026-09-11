package lk.ac.ruhuna.dcs.cvmanagement.modules.adminstudents.api.dto.response;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;
import lk.ac.ruhuna.dcs.cvmanagement.shared.api.dto.FileAssetResponse;

/** Read-only Student certificate item used by the Admin deep-dive. */
public record AdminCertificateResponse(
        UUID id,
        String title,
        String issuer,
        LocalDate issueDate,
        String credentialUrl,
        boolean cvInclude,
        FileAssetResponse evidence,
        long version,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt) {
}
