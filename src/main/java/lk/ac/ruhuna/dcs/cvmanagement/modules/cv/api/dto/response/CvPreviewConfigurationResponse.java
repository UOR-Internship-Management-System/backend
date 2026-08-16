package lk.ac.ruhuna.dcs.cvmanagement.modules.cv.api.dto.response;

import java.util.List;
import java.util.UUID;

public record CvPreviewConfigurationResponse(
    List<UUID> includedExperienceIds,
    List<UUID> includedProjectIds,
    List<UUID> includedCertificateIds,
    List<UUID> includedAwardIds,
    List<UUID> includedActivityIds) {
}
