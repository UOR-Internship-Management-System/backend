package lk.ac.ruhuna.dcs.cvmanagement.modules.verification.api.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record StudentVerificationStartRequest(
        @NotBlank @Size(max = 160) String fullName,
        @NotBlank @Size(max = 32) @Pattern(regexp = "^[A-Za-z]{2}/\\d{4}/\\d{5}$") String indexNumber,
        @NotBlank @Email @Size(max = 254) String universityEmail) {
}
