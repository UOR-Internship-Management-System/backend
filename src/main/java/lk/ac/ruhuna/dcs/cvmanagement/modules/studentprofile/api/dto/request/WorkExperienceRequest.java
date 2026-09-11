package lk.ac.ruhuna.dcs.cvmanagement.modules.studentprofile.api.dto.request;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;

public record WorkExperienceRequest(
    @NotBlank @Size(max = 200) String organization,
    @NotBlank @Size(max = 150) String positionTitle,
    @Size(max = 150) String location,
    @NotNull LocalDate startDate,
    LocalDate endDate,
    boolean currentRole,
    @Size(max = 4000) String description,
    Boolean cvInclude) {

    @AssertTrue(message = "End date cannot be before start date.")
    public boolean isDateRangeValid() {
        return startDate == null || endDate == null || !endDate.isBefore(startDate);
    }

    @AssertTrue(message = "End date must be null for a current role and is required otherwise.")
    public boolean isCurrentRoleEndDateValid() {
        return currentRole ? endDate == null : endDate != null;
    }
}
