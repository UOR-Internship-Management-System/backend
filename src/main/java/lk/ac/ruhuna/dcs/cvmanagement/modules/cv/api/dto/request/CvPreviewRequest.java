package lk.ac.ruhuna.dcs.cvmanagement.modules.cv.api.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;
import java.util.UUID;
import lk.ac.ruhuna.dcs.cvmanagement.modules.cv.domain.validation.UniqueUuidList;

public record CvPreviewRequest(
        @NotNull @Size(max = 100) @UniqueUuidList List<@NotNull UUID> includedExperienceIds,
        @NotNull @Size(max = 100) @UniqueUuidList List<@NotNull UUID> includedProjectIds,
        @NotNull @Size(max = 100) @UniqueUuidList List<@NotNull UUID> includedCertificateIds,
        @NotNull @Size(max = 100) @UniqueUuidList List<@NotNull UUID> includedAwardIds,
        @NotNull @Size(max = 100) @UniqueUuidList List<@NotNull UUID> includedActivityIds) {
}
