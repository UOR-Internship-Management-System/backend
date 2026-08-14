package lk.ac.ruhuna.dcs.cvmanagement.modules.adminstudents.api.dto.response;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;

/** Read-only Student award item used by the Admin deep-dive. */
public record AdminAwardResponse(
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
