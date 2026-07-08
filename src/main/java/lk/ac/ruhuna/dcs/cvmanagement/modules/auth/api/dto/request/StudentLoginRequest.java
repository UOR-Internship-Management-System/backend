package lk.ac.ruhuna.dcs.cvmanagement.modules.auth.api.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record StudentLoginRequest(
        @NotBlank @Email @Size(max = 254) String universityEmail,
        @NotBlank @Size(min = 8, max = 128) String password) {
}
