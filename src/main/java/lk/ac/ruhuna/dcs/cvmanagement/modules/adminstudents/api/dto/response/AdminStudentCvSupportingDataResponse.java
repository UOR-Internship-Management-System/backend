package lk.ac.ruhuna.dcs.cvmanagement.modules.adminstudents.api.dto.response;

import java.util.List;

/** Read-only CV-supporting Student data returned in the Admin deep-dive. */
public record AdminStudentCvSupportingDataResponse(
        List<AdminExperienceResponse> experiences,
        List<AdminCertificateResponse> certificates,
        List<AdminAwardResponse> awards,
        List<AdminActivityResponse> activities) {

    public AdminStudentCvSupportingDataResponse {
        experiences = List.copyOf(experiences);
        certificates = List.copyOf(certificates);
        awards = List.copyOf(awards);
        activities = List.copyOf(activities);
    }
}
