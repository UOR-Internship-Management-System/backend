package lk.ac.ruhuna.dcs.cvmanagement.modules.cv.domain;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Component;

@Component
public class CvPreviewCache {

    public record CachedPreview(
        UUID studentId,
        String htmlPreview,
        List<UUID> includedExperienceIds,
        List<UUID> includedProjectIds,
        List<UUID> includedCertificateIds,
        List<UUID> includedAwardIds,
        List<UUID> includedActivityIds,
        OffsetDateTime generatedAt,
        OffsetDateTime expiresAt) {
    }

    private final Map<UUID, CachedPreview> previews = new ConcurrentHashMap<>();

    public UUID store(CachedPreview preview, UUID previewId) {
        previews.put(previewId, preview);
        return previewId;
    }

    public CachedPreview get(UUID previewId) {
        CachedPreview preview = previews.get(previewId);
        if (preview == null || preview.expiresAt().isBefore(OffsetDateTime.now())) {
            previews.remove(previewId);
            return null;
        }
        return preview;
    }

    public void remove(UUID previewId) {
        previews.remove(previewId);
    }
}
