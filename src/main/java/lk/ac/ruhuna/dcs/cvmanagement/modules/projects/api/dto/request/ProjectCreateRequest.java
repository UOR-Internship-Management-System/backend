package lk.ac.ruhuna.dcs.cvmanagement.modules.projects.api.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record ProjectCreateRequest(
    @NotBlank @Size(max = 200) String title,
    String description,
    String repositoryUrl,
    String demoUrl,
    LocalDate startDate,
    LocalDate endDate,
    List<UUID> skillIds,
    boolean includeInCv) {
}
