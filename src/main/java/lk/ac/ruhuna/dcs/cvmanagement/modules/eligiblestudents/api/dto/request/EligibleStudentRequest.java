package lk.ac.ruhuna.dcs.cvmanagement.modules.eligiblestudents.api.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record EligibleStudentRequest(
    @NotBlank @Pattern(
        regexp = "^[A-Za-z]{2}/[0-9]{4}/[0-9]{5}$",
        message = "Index number must look like CS/2022/00123.")
    String indexNumber,
    @NotBlank @Size(max = 254) @Pattern(
        regexp = "^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$",
        message = "University email must be a valid email address.")
    String universityEmail,
    @NotBlank @Size(max = 160) String fullName,
    @NotNull @Min(3) @Max(4) Short academicLevel) {
}
