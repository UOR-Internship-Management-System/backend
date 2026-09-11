package lk.ac.ruhuna.dcs.cvmanagement.modules.internships.api.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;
import java.util.UUID;

/** Request body for atomically creating Internship Request metadata and required skills. */
public record InternshipRequestCreateRequest(
        @NotNull(message = "companyId is required.") UUID companyId,
        @NotBlank(message = "Role title is required.")
        @Size(max = 200, message = "Role title must not exceed 200 characters.") String title,
        @Size(max = 10000, message = "Description must not exceed 10000 characters.") String description,
        @Min(value = 0, message = "shortlistGuidanceValue must be between 0 and 10000.")
        @Max(value = 10000, message = "shortlistGuidanceValue must be between 0 and 10000.") Integer shortlistGuidanceValue,
        @NotNull(message = "requiredSkills is required.")
        @Size(max = 100, message = "At most 100 required skills may be selected.")
        List<@Valid InternshipRequiredSkillRequest> requiredSkills) {
}
