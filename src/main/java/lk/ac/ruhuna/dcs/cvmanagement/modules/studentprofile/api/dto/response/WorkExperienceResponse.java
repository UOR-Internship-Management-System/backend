package lk.ac.ruhuna.dcs.cvmanagement.modules.studentprofile.api.dto.response;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;

public record WorkExperienceResponse(
    UUID id,
    String organization,
    String positionTitle,
    String location,
    LocalDate startDate,
    LocalDate endDate,
    boolean currentRole,
    String description,
    boolean cvInclude,
    long version,
    OffsetDateTime createdAt,
    OffsetDateTime updatedAt) {
}
