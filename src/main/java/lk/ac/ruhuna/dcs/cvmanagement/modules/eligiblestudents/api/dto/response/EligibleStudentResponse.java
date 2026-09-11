package lk.ac.ruhuna.dcs.cvmanagement.modules.eligiblestudents.api.dto.response;

import java.time.OffsetDateTime;
import java.util.UUID;

public record EligibleStudentResponse(
    UUID id,
    String indexNumber,
    String universityEmail,
    String fullName,
    short academicLevel,
    boolean active,
    boolean registered,
    OffsetDateTime createdAt,
    OffsetDateTime updatedAt) {
}
