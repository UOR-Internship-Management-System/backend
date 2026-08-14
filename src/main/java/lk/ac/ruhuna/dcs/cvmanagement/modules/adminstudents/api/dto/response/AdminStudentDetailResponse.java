package lk.ac.ruhuna.dcs.cvmanagement.modules.adminstudents.api.dto.response;

import java.util.Objects;

/** Complete read-only Student deep-dive summary. */
public record AdminStudentDetailResponse(
        AdminStudentListItemResponse student,
        AdminStudentProfileResponse profile,
        AdminStudentCvSupportingDataResponse cvSupportingData,
        AdminLatestCvResponse latestCv) {

    public AdminStudentDetailResponse {
        Objects.requireNonNull(student, "student");
        Objects.requireNonNull(profile, "profile");
        Objects.requireNonNull(cvSupportingData, "cvSupportingData");
        Objects.requireNonNull(latestCv, "latestCv");
    }
}
