package lk.ac.ruhuna.dcs.cvmanagement.modules.studentprofile.api.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;

public record CertificateRequest(
    @NotBlank @Size(max = 200) String title,
    @NotBlank @Size(max = 200) String issuer,
    @NotNull LocalDate issueDate,
    @Size(max = 2048) String credentialUrl,
    Boolean cvInclude) {
}
