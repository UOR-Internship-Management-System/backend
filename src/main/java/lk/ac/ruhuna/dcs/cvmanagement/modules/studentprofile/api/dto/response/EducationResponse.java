package lk.ac.ruhuna.dcs.cvmanagement.modules.studentprofile.api.dto.response;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;

public record EducationResponse(
    UUID id,
    String degree,
    String institution,
    String institutionUrl,
    String location,
    LocalDate startDate,
    LocalDate endDate,
    boolean current,
    String resultNote,
    boolean cvInclude,
    long version,
    OffsetDateTime createdAt,
    OffsetDateTime updatedAt) {
}
