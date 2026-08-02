package lk.ac.ruhuna.dcs.cvmanagement.modules.studentprofile.api.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;

public record ActivityRequest(
    @NotBlank @Size(max = 200) String activityName,
    @Size(max = 150) String roleTitle,
    LocalDate startDate,
    LocalDate endDate,
    @Size(max = 4000) String description,
    Boolean cvInclude) {
}
