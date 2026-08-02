package lk.ac.ruhuna.dcs.cvmanagement.modules.studentprofile.api.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;

public record AwardRequest(
    @NotBlank @Size(max = 200) String title,
    @Size(max = 200) String issuer,
    LocalDate awardDate,
    @Size(max = 4000) String description,
    Boolean cvInclude) {
}
