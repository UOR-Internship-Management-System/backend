package lk.ac.ruhuna.dcs.cvmanagement.modules.studentprofile.api.dto.response;

import java.time.LocalDate;
import java.util.UUID;

public record WorkExperienceResponse(
    UUID id,
    String organization,
    String positionTitle,
    LocalDate startDate,
    LocalDate endDate,
    String description,
    boolean cvInclude) {
}
