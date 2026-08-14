package lk.ac.ruhuna.dcs.cvmanagement.modules.adminstudents.persistence.projection;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;

/** Immutable read projection for one Student work-experience record. */
public record AdminExperienceRow(
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
