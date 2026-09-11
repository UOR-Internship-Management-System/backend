package lk.ac.ruhuna.dcs.cvmanagement.modules.studentprofile.api.dto.response;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;

public record AwardResponse(
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
