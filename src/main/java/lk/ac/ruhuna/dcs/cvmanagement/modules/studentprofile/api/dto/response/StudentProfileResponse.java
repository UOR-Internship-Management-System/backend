package lk.ac.ruhuna.dcs.cvmanagement.modules.studentprofile.api.dto.response;

import java.util.UUID;

public record StudentProfileResponse(
    UUID studentId,
    String fullName,
    String indexNumber,
    String universityEmail,
    String summary,
    String phone,
    String profilePhotoUrl) {
}
