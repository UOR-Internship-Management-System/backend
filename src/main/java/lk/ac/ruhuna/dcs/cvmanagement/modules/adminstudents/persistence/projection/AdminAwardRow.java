package lk.ac.ruhuna.dcs.cvmanagement.modules.adminstudents.persistence.projection;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;

/** Immutable read projection for one Student award record. */
public record AdminAwardRow(
        UUID id,
        String title,
        String issuer,
        LocalDate awardDate,
        String description,
        boolean cvInclude,
        long version,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt) {
}
