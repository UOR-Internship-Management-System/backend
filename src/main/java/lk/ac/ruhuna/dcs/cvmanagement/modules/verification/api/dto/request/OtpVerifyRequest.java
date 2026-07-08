package lk.ac.ruhuna.dcs.cvmanagement.modules.verification.api.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record OtpVerifyRequest(
        @NotBlank @Pattern(regexp = "^\\d{6}$") String otp) {
}
