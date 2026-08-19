package lk.ac.ruhuna.dcs.cvmanagement.modules.cv.application;

import java.time.Clock;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import lk.ac.ruhuna.dcs.cvmanagement.infrastructure.storage.FileStoragePort;
import lk.ac.ruhuna.dcs.cvmanagement.modules.cv.persistence.repository.CvPreviewRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Reclaims expired unconsumed preview PDFs without ever touching consumed/active CV files. */
@Service
public class CvPreviewCleanupService {

    private static final Logger LOGGER = LoggerFactory.getLogger(CvPreviewCleanupService.class);
    private final CvPreviewRepository previewRepository;
    private final FileStoragePort cvFileStorage;
    private final Clock clock;
    private final Duration consumedPreviewRetention;

    public CvPreviewCleanupService(
            CvPreviewRepository previewRepository,
            @Qualifier("cvFileStorage") FileStoragePort cvFileStorage,
            Clock clock,
            @Value("${app.cv.consumed-preview-retention:PT24H}") Duration consumedPreviewRetention) {
        this.previewRepository = previewRepository;
        this.cvFileStorage = cvFileStorage;
        this.clock = clock;
        if (consumedPreviewRetention.isNegative() || consumedPreviewRetention.isZero()) {
            throw new IllegalArgumentException("CV consumed-preview retention must be positive");
        }
        this.consumedPreviewRetention = consumedPreviewRetention;
    }

    @Scheduled(fixedDelayString = "${app.cv.cleanup.poll-delay-ms:60000}")
    @Transactional
    public void deleteExpiredUnconsumedPreviews() {
        OffsetDateTime now = OffsetDateTime.ofInstant(clock.instant(), ZoneOffset.UTC);
        for (var preview : previewRepository.findTop100ByConsumedAtIsNullAndExpiresAtBeforeOrderByExpiresAtAsc(now)) {
            try {
                if (preview.getStagedStorageKey() != null) cvFileStorage.delete(preview.getStagedStorageKey());
                previewRepository.deleteById(preview.getPreviewId());
            } catch (RuntimeException exception) {
                LOGGER.warn("Expired CV preview cleanup failed for preview {}.", preview.getPreviewId());
            }
        }

        OffsetDateTime consumedCutoff = now.minus(consumedPreviewRetention);
        for (var preview : previewRepository.findTop100ByConsumedAtIsNotNullAndConsumedAtBeforeOrderByConsumedAtAsc(consumedCutoff)) {
            try {
                // A consumed preview may reference the active PDF storage key. The active CV/file-asset
                // lifecycle owns that object, so only preview metadata is removed here.
                previewRepository.deleteById(preview.getPreviewId());
            } catch (RuntimeException exception) {
                LOGGER.warn("Consumed CV preview metadata cleanup failed for preview {}.", preview.getPreviewId());
            }
        }
    }
}
