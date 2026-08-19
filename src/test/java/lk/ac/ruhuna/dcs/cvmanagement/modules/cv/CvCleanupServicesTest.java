package lk.ac.ruhuna.dcs.cvmanagement.modules.cv;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lk.ac.ruhuna.dcs.cvmanagement.infrastructure.storage.FileAssetEntity;
import lk.ac.ruhuna.dcs.cvmanagement.infrastructure.storage.FileAssetRepository;
import lk.ac.ruhuna.dcs.cvmanagement.infrastructure.storage.FileStoragePort;
import lk.ac.ruhuna.dcs.cvmanagement.modules.cv.application.CvOrphanFileCleanupService;
import lk.ac.ruhuna.dcs.cvmanagement.modules.cv.application.CvPreviewCleanupService;
import lk.ac.ruhuna.dcs.cvmanagement.modules.cv.persistence.entity.CvPreviewEntity;
import lk.ac.ruhuna.dcs.cvmanagement.modules.cv.persistence.repository.CvPreviewRepository;
import lk.ac.ruhuna.dcs.cvmanagement.modules.cv.persistence.repository.CvRepository;
import org.junit.jupiter.api.Test;

class CvCleanupServicesTest {

    private static final Instant NOW = Instant.parse("2026-08-19T06:00:00Z");
    private static final Clock CLOCK = Clock.fixed(NOW, ZoneOffset.UTC);

    @Test
    void expiredUnconsumedPreviewDeletesStagedObjectAndMetadata() {
        CvPreviewRepository previews = mock(CvPreviewRepository.class);
        FileStoragePort storage = mock(FileStoragePort.class);
        CvPreviewEntity preview = new CvPreviewEntity();
        UUID previewId = UUID.randomUUID();
        preview.setPreviewId(previewId);
        preview.setStagedStorageKey("cv/objects/2026/08/expired.pdf");
        preview.setExpiresAt(OffsetDateTime.ofInstant(NOW.minusSeconds(1), ZoneOffset.UTC));
        when(previews.findTop100ByConsumedAtIsNullAndExpiresAtBeforeOrderByExpiresAtAsc(
                OffsetDateTime.ofInstant(NOW, ZoneOffset.UTC))).thenReturn(List.of(preview));
        when(previews.findTop100ByConsumedAtIsNotNullAndConsumedAtBeforeOrderByConsumedAtAsc(
                OffsetDateTime.ofInstant(NOW.minus(Duration.ofHours(24)), ZoneOffset.UTC))).thenReturn(List.of());

        new CvPreviewCleanupService(previews, storage, CLOCK, Duration.ofHours(24))
                .deleteExpiredUnconsumedPreviews();

        verify(storage).delete("cv/objects/2026/08/expired.pdf");
        verify(previews).deleteById(previewId);
    }

    @Test
    void consumedPreviewCleanupNeverDeletesActivePdfObject() {
        CvPreviewRepository previews = mock(CvPreviewRepository.class);
        FileStoragePort storage = mock(FileStoragePort.class);
        CvPreviewEntity preview = new CvPreviewEntity();
        UUID previewId = UUID.randomUUID();
        preview.setPreviewId(previewId);
        preview.setConsumedAt(OffsetDateTime.ofInstant(NOW.minus(Duration.ofDays(2)), ZoneOffset.UTC));
        preview.setStagedStorageKey("cv/objects/2026/08/active.pdf");
        OffsetDateTime now = OffsetDateTime.ofInstant(NOW, ZoneOffset.UTC);
        when(previews.findTop100ByConsumedAtIsNullAndExpiresAtBeforeOrderByExpiresAtAsc(now)).thenReturn(List.of());
        when(previews.findTop100ByConsumedAtIsNotNullAndConsumedAtBeforeOrderByConsumedAtAsc(
                now.minusHours(24))).thenReturn(List.of(preview));

        new CvPreviewCleanupService(previews, storage, CLOCK, Duration.ofHours(24))
                .deleteExpiredUnconsumedPreviews();

        verify(previews).deleteById(previewId);
        verify(storage, never()).delete("cv/objects/2026/08/active.pdf");
    }

    @Test
    void orphanCleanupDeletesOnlyUnreferencedCvOwnedAssets() {
        CvRepository cvs = mock(CvRepository.class);
        FileAssetRepository assets = mock(FileAssetRepository.class);
        FileStoragePort storage = mock(FileStoragePort.class);
        FileAssetEntity orphan = mock(FileAssetEntity.class);
        UUID assetId = UUID.randomUUID();
        when(orphan.getId()).thenReturn(assetId);
        when(orphan.getStorageKey()).thenReturn("cv/objects/2026/08/orphan.pdf");
        OffsetDateTime cutoff = OffsetDateTime.ofInstant(NOW, ZoneOffset.UTC).minusHours(1);
        when(assets.findTop100ByStorageKeyStartingWithAndCreatedAtBeforeOrderByCreatedAtAsc("cv/objects/", cutoff))
                .thenReturn(List.of(orphan));
        when(cvs.existsByPdfFileAssetId(assetId)).thenReturn(false);

        new CvOrphanFileCleanupService(cvs, assets, storage, CLOCK, Duration.ofHours(1)).retryOrphanCleanup();

        verify(storage).delete("cv/objects/2026/08/orphan.pdf");
        verify(assets).deleteById(assetId);
    }

    @Test
    void explicitCleanupDoesNotDeleteReferencedActiveAsset() {
        CvRepository cvs = mock(CvRepository.class);
        FileAssetRepository assets = mock(FileAssetRepository.class);
        FileStoragePort storage = mock(FileStoragePort.class);
        UUID assetId = UUID.randomUUID();
        when(cvs.existsByPdfFileAssetId(assetId)).thenReturn(true);

        new CvOrphanFileCleanupService(cvs, assets, storage, CLOCK, Duration.ofHours(1))
                .deleteIfUnreferenced(assetId);

        verify(assets, never()).findById(assetId);
        verify(storage, never()).delete(org.mockito.ArgumentMatchers.anyString());
    }
}
