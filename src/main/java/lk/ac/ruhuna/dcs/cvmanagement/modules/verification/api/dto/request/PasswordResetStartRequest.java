package lk.ac.ruhuna.dcs.cvmanagement.modules.verification.api.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lk.ac.ruhuna.dcs.cvmanagement.shared.security.AccountType;

public record PasswordResetStartRequest(
        @NotNull AccountType accountType,
        @NotBlank @Email @Size(max = 254) String email) {
}
