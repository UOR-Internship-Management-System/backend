package lk.ac.ruhuna.dcs.cvmanagement.modules.studentprofile.api.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;

public record WorkExperienceRequest(
    @NotBlank @Size(max = 200) String organization,
    @Size(max = 150) String positionTitle,
    @Size(max = 150) String location,
    LocalDate startDate,
    LocalDate endDate,
    boolean currentRole,
    @Size(max = 4000) String description,
    Boolean cvInclude) {
}
