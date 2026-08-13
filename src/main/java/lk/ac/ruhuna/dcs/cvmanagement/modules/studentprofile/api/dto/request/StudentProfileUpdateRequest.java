package lk.ac.ruhuna.dcs.cvmanagement.modules.studentprofile.api.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;

public record StudentProfileUpdateRequest(
    @Size(max = 150) String fullName,
    @Email @Size(max = 254) String personalEmail,
    @Size(max = 200) String headline,
    String summary,
    @Size(max = 30) String phone,
    @Size(max = 150) String location) {
}
