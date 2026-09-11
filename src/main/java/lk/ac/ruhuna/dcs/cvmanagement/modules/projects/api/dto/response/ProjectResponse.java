package lk.ac.ruhuna.dcs.cvmanagement.modules.projects.api.dto.response;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

public record ProjectResponse(
    UUID projectId,
    String title,
    String description,
    String repositoryUrl,
    String demoUrl,
    LocalDate startDate,
    LocalDate endDate,
    List<ProjectSkillResponse> skills,
    boolean includeInCv,
    long version,
    OffsetDateTime createdAt,
    OffsetDateTime updatedAt) {
}
