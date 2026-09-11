package lk.ac.ruhuna.dcs.cvmanagement.modules.cv.domain.model;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import lk.ac.ruhuna.dcs.cvmanagement.modules.cv.api.dto.request.CvPreviewRequest;
import lk.ac.ruhuna.dcs.cvmanagement.modules.cv.api.dto.response.CvPreviewConfigurationResponse;
import lk.ac.ruhuna.dcs.cvmanagement.modules.cv.domain.exception.CvConfigurationInvalidException;

/** Immutable, canonical record-level inclusion snapshot. Request order never controls CV output order. */
public record CvConfiguration(
        List<UUID> includedExperienceIds,
        List<UUID> includedProjectIds,
        List<UUID> includedCertificateIds,
        List<UUID> includedAwardIds,
        List<UUID> includedActivityIds) {

    private static final Comparator<UUID> UUID_ORDER = Comparator.comparing(UUID::toString);

    public CvConfiguration {
        includedExperienceIds = canonical(includedExperienceIds);
        includedProjectIds = canonical(includedProjectIds);
        includedCertificateIds = canonical(includedCertificateIds);
        includedAwardIds = canonical(includedAwardIds);
        includedActivityIds = canonical(includedActivityIds);
    }

    public static CvConfiguration from(CvPreviewRequest request) {
        Objects.requireNonNull(request, "request must not be null");
        return new CvConfiguration(
                request.includedExperienceIds(),
                request.includedProjectIds(),
                request.includedCertificateIds(),
                request.includedAwardIds(),
                request.includedActivityIds());
    }

    public CvPreviewConfigurationResponse toResponse() {
        return new CvPreviewConfigurationResponse(
                includedExperienceIds,
                includedProjectIds,
                includedCertificateIds,
                includedAwardIds,
                includedActivityIds);
    }

    private static List<UUID> canonical(List<UUID> values) {
        if (values == null || values.size() > 100 || values.stream().anyMatch(Objects::isNull)) {
            throw new CvConfigurationInvalidException();
        }
        ArrayList<UUID> sorted = new ArrayList<>(values);
        sorted.sort(UUID_ORDER);
        for (int i = 1; i < sorted.size(); i++) {
            if (sorted.get(i - 1).equals(sorted.get(i))) {
                throw new CvConfigurationInvalidException();
            }
        }
        return List.copyOf(sorted);
    }
}
