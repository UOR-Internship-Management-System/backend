package lk.ac.ruhuna.dcs.cvmanagement.modules.adminstudents.api.dto.response;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

/** Read-only Student project projection used by Admin inspection. */
public record AdminProjectResponse(
        UUID projectId,
        String title,
        String description,
        String repositoryUrl,
        String demoUrl,
        LocalDate startDate,
        LocalDate endDate,
        List<AdminProjectSkillResponse> skills,
        boolean includeInCv,
        long version,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt) {

    public AdminProjectResponse {
        skills = List.copyOf(skills);
    }
}
