package lk.ac.ruhuna.dcs.cvmanagement.modules.studentprofile.api.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ContactLinkRequest(
    @NotBlank @Size(max = 60) String label,
    @NotBlank @Size(max = 2048) String url,
    Integer displayOrder,
    Boolean cvInclude) {
}
