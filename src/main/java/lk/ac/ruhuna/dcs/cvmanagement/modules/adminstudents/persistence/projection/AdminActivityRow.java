package lk.ac.ruhuna.dcs.cvmanagement.modules.adminstudents.persistence.projection;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;

/** Immutable read projection for one Student activity record. */
public record AdminActivityRow(
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
