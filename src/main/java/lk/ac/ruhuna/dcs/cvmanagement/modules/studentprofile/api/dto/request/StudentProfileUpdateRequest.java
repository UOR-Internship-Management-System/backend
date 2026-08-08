package lk.ac.ruhuna.dcs.cvmanagement.modules.studentprofile.api.dto.request;

import jakarta.validation.constraints.Size;
import java.util.UUID;

public record StudentProfileUpdateRequest(
    @Size(max = 150) String fullName,
    @Size(max = 4000) String summary,
    @Size(max = 30) String phone,
    UUID profilePhotoFileId) {
}
