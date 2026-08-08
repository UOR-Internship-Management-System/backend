package lk.ac.ruhuna.dcs.cvmanagement.modules.studentprofile.api.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;

public record CertificateRequest(
    @NotBlank @Size(max = 200) String title,
    @Size(max = 200) String issuer,
    LocalDate issueDate,
    @Size(max = 2048) String credentialUrl,
    Boolean cvInclude) {
}
