package lk.ac.ruhuna.dcs.cvmanagement.modules.cv.api.dto.request;

import java.util.List;
import java.util.UUID;

public record CvPreviewRequest(
    List<UUID> includedExperienceIds,
    List<UUID> includedProjectIds,
    List<UUID> includedCertificateIds,
    List<UUID> includedAwardIds,
    List<UUID> includedActivityIds) {
}
