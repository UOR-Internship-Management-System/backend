package lk.ac.ruhuna.dcs.cvmanagement.modules.studentprofile.api.dto.response;

import java.time.LocalDate;
import java.util.UUID;

public record CertificateResponse(
    UUID id,
    String title,
    String issuer,
    LocalDate issueDate,
    String credentialUrl,
    boolean cvInclude) {
}
