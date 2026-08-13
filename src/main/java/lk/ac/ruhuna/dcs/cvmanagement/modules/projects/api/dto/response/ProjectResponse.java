package lk.ac.ruhuna.dcs.cvmanagement.modules.projects.api.dto.response;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
import lk.ac.ruhuna.dcs.cvmanagement.modules.skills.api.dto.response.IndividualSkillResponse;

public record ProjectResponse(
    UUID projectId,
    String title,
    String description,
    String repositoryUrl,
    String demoUrl,
    LocalDate startDate,
    LocalDate endDate,
    List<IndividualSkillResponse> skills,
    boolean includeInCv,
    long version,
    OffsetDateTime createdAt,
    OffsetDateTime updatedAt) {
}
