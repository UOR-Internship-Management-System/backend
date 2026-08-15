package lk.ac.ruhuna.dcs.cvmanagement.modules.internships.api.dto.response;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

/** Public Internship Request response; GPA and lifecycle status are intentionally absent. */
public record InternshipRequestResponse(
        UUID requestId,
        InternshipCompanyResponse company,
        String title,
        String description,
        Integer shortlistGuidanceValue,
        List<InternshipRequiredSkillResponse> requiredSkills,
        long version,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt) {
}
