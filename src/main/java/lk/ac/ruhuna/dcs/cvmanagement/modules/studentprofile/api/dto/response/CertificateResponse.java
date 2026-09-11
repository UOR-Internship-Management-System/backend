package lk.ac.ruhuna.dcs.cvmanagement.modules.studentprofile.api.dto.response;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;
import lk.ac.ruhuna.dcs.cvmanagement.shared.api.dto.FileAssetResponse;

public record CertificateResponse(
    UUID id,
    String title,
    String issuer,
    LocalDate issueDate,
    String credentialUrl,
    FileAssetResponse evidence,
    boolean cvInclude,
    long version,
    OffsetDateTime createdAt,
    OffsetDateTime updatedAt) {
}
