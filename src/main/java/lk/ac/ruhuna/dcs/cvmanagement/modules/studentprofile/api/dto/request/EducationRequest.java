package lk.ac.ruhuna.dcs.cvmanagement.modules.studentprofile.api.dto.request;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;

public record EducationRequest(
    @NotBlank @Size(max = 200) String degree,
    @NotBlank @Size(max = 200) String institution,
    @Size(max = 2048) String institutionUrl,
    @Size(max = 150) String location,
    LocalDate startDate,
    LocalDate endDate,
    boolean current,
    @Size(max = 500) String resultNote,
    Boolean cvInclude) {

    @AssertTrue(message = "End date cannot be before start date.")
    public boolean isDateRangeValid() {
        return startDate == null || endDate == null || !endDate.isBefore(startDate);
    }

    @AssertTrue(message = "End date must be empty while marked as in progress.")
    public boolean isCurrentEndDateValid() {
        return !current || endDate == null;
    }
}
