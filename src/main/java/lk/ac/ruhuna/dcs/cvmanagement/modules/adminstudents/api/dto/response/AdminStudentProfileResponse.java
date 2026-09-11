package lk.ac.ruhuna.dcs.cvmanagement.modules.adminstudents.api.dto.response;

import java.time.OffsetDateTime;
import java.util.UUID;
import lk.ac.ruhuna.dcs.cvmanagement.shared.api.dto.FileAssetResponse;

/** Read-only Student profile projection used by the Admin deep-dive. */
public record AdminStudentProfileResponse(
        UUID studentId,
        String fullName,
        String indexNumber,
        String universityEmail,
        String degreeProgramme,
        int studentLevel,
        Integer cohortYear,
        String personalEmail,
        String headline,
        String summary,
        String phone,
        String location,
        FileAssetResponse profilePhoto,
        long version,
        OffsetDateTime updatedAt,
        OffsetDateTime cvSourceUpdatedAt) {
}
