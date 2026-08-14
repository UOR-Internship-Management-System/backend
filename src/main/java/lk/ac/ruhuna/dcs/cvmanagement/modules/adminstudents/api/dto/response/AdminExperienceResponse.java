package lk.ac.ruhuna.dcs.cvmanagement.modules.adminstudents.api.dto.response;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;

/** Read-only Student work-experience item used by the Admin deep-dive. */
public record AdminExperienceResponse(
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
