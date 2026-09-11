package lk.ac.ruhuna.dcs.cvmanagement.modules.adminstudents.persistence.projection;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;

/** Immutable database projection for one Student-owned portfolio project. */
public record AdminProjectRow(
        UUID projectId,
        String title,
        String description,
        String repositoryUrl,
        String demoUrl,
        LocalDate startDate,
        LocalDate endDate,
        boolean includeInCv,
        long version,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt) {
}
