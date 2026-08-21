package lk.ac.ruhuna.dcs.cvmanagement.modules.cv.application;

import java.time.Clock;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.UUID;
import lk.ac.ruhuna.dcs.cvmanagement.infrastructure.storage.FileAssetRepository;
import lk.ac.ruhuna.dcs.cvmanagement.infrastructure.storage.FileStoragePort;
import lk.ac.ruhuna.dcs.cvmanagement.modules.cv.persistence.repository.CvRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Best-effort post-commit reclamation of superseded CV file assets. */
@Service
public class CvOrphanFileCleanupService {
    private static final Logger LOGGER = LoggerFactory.getLogger(CvOrphanFileCleanupService.class);
    private final CvRepository cvRepository;
    private final FileAssetRepository fileAssetRepository;
    private final FileStoragePort cvFileStorage;
    private final Clock clock;
    private final Duration orphanGracePeriod;

    public CvOrphanFileCleanupService(
            CvRepository cvRepository,
            FileAssetRepository fileAssetRepository,
            @Qualifier("cvFileStorage") FileStoragePort cvFileStorage,
            Clock clock,
            @Value("${app.cv.cleanup.orphan-grace-period:PT1H}") Duration orphanGracePeriod) {
        this.cvRepository = cvRepository;
        this.fileAssetRepository = fileAssetRepository;
        this.cvFileStorage = cvFileStorage;
        this.clock = clock;
        if (orphanGracePeriod.isNegative() || orphanGracePeriod.isZero()) {
            throw new IllegalArgumentException("CV orphan-file grace period must be positive");
        }
        this.orphanGracePeriod = orphanGracePeriod;
    }

    @Scheduled(fixedDelayString = "${app.cv.cleanup.poll-delay-ms:60000}")
    @Transactional
    public void retryOrphanCleanup() {
        OffsetDateTime cutoff = OffsetDateTime.ofInstant(clock.instant(), ZoneOffset.UTC).minus(orphanGracePeriod);
        for (var asset : fileAssetRepository
                .findTop100ByStorageKeyStartingWithAndCreatedAtBeforeOrderByCreatedAtAsc("cv/objects/", cutoff)) {
            if (!cvRepository.existsByPdfFileAssetId(asset.getId())) {
                deleteAsset(asset.getId(), asset.getStorageKey());
            }
        }
    }

    @Transactional
    public void deleteIfUnreferenced(UUID fileAssetId) {
        if (fileAssetId == null || cvRepository.existsByPdfFileAssetId(fileAssetId)) return;
        fileAssetRepository.findById(fileAssetId)
                .ifPresent(asset -> deleteAsset(asset.getId(), asset.getStorageKey()));
    }

    private void deleteAsset(UUID fileAssetId, String storageKey) {
        try {
            cvFileStorage.delete(storageKey);
            fileAssetRepository.deleteById(fileAssetId);
        } catch (RuntimeException exception) {
            LOGGER.warn("Superseded CV file cleanup failed for file asset {}.", fileAssetId);
        }
    }
}
