package lk.ac.ruhuna.dcs.cvmanagement.modules.studentprofile.api.dto.response;

import java.time.LocalDate;
import java.util.UUID;

public record ActivityResponse(
    UUID id,
    String activityName,
    String roleTitle,
    LocalDate startDate,
    LocalDate endDate,
    String description,
    boolean cvInclude) {
}
