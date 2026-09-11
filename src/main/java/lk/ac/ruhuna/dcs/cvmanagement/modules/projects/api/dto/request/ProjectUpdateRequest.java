package lk.ac.ruhuna.dcs.cvmanagement.modules.projects.api.dto.request;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record ProjectUpdateRequest(
    @Size(max = 200) String title,
    String description,
    String repositoryUrl,
    String demoUrl,
    LocalDate startDate,
    LocalDate endDate,
    List<@NotNull UUID> skillIds,
    Boolean includeInCv) {

    @AssertTrue(message = "End date cannot be before start date.")
    public boolean isDateRangeValid() {
        return startDate == null || endDate == null || !endDate.isBefore(startDate);
    }
}
