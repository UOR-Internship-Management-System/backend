package lk.ac.ruhuna.dcs.cvmanagement.modules.adminstudents.api.dto.response;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;

/** Read-only Student activity item used by the Admin deep-dive. */
public record AdminActivityResponse(
        UUID id,
        String activityName,
        String roleTitle,
        LocalDate startDate,
        LocalDate endDate,
        String description,
        boolean cvInclude,
        long version,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt) {
}
