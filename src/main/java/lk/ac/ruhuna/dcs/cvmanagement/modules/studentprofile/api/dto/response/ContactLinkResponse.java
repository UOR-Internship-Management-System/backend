package lk.ac.ruhuna.dcs.cvmanagement.modules.studentprofile.api.dto.response;

import java.util.UUID;

public record ContactLinkResponse(
    UUID id,
    String label,
    String url,
    Integer displayOrder,
    boolean cvInclude) {
}
