package lk.ac.ruhuna.dcs.cvmanagement.modules.cv;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Optional;
import java.util.UUID;
import lk.ac.ruhuna.dcs.cvmanagement.infrastructure.storage.FileAssetEntity;
import lk.ac.ruhuna.dcs.cvmanagement.infrastructure.storage.FileAssetRepository;
import lk.ac.ruhuna.dcs.cvmanagement.modules.cv.application.ActiveCvFileResolverService;
import lk.ac.ruhuna.dcs.cvmanagement.modules.cv.application.CvFileIntegrityService;
import lk.ac.ruhuna.dcs.cvmanagement.modules.cv.domain.exception.CvFileUnavailableException;
import lk.ac.ruhuna.dcs.cvmanagement.modules.cv.persistence.entity.CvEntity;
import lk.ac.ruhuna.dcs.cvmanagement.modules.cv.persistence.repository.CvRepository;
import org.junit.jupiter.api.Test;

class ActiveCvFileResolverServiceTest {

    private final CvRepository cvRepository = mock(CvRepository.class);
    private final FileAssetRepository fileAssetRepository = mock(FileAssetRepository.class);
    private final CvFileIntegrityService integrityService = mock(CvFileIntegrityService.class);
    private final ActiveCvFileResolverService service =
            new ActiveCvFileResolverService(cvRepository, fileAssetRepository, integrityService);

    @Test
    void resolvesOnlyThePersistedPdfAfterIntegrityVerification() {
        UUID studentId = UUID.randomUUID();
        UUID cvId = UUID.randomUUID();
        UUID assetId = UUID.randomUUID();
        byte[] bytes = "%PDF-1.7\nbody".getBytes(java.nio.charset.StandardCharsets.US_ASCII);
        CvEntity cv = activeCv(cvId, studentId, assetId, bytes.length);
        FileAssetEntity asset = mock(FileAssetEntity.class);
        when(asset.getMimeType()).thenReturn("application/pdf");
        when(asset.getFileName()).thenReturn("cv-student.pdf");
        when(asset.getFileSizeBytes()).thenReturn((long) bytes.length);
        when(asset.getStorageKey()).thenReturn("cv/objects/2026/08/object.pdf");
        when(asset.getChecksumSha256()).thenReturn("a".repeat(64));
        when(cvRepository.findActiveByStudentId(studentId)).thenReturn(Optional.of(cv));
        when(fileAssetRepository.findById(assetId)).thenReturn(Optional.of(asset));
        when(integrityService.readVerified(asset.getStorageKey(), bytes.length, "a".repeat(64))).thenReturn(bytes);

        var resolved = service.resolve(studentId);

        assertThat(resolved.cvId()).isEqualTo(cvId);
        assertThat(resolved.revision()).isEqualTo(3);
        assertThat(resolved.fileName()).isEqualTo("cv-student.pdf");
        assertThat(resolved.bytes()).isEqualTo(bytes);
    }

    @Test
    void metadataMismatchFailsClosedWithoutReturningStoredBytes() {
        UUID studentId = UUID.randomUUID();
        UUID assetId = UUID.randomUUID();
        CvEntity cv = activeCv(UUID.randomUUID(), studentId, assetId, 64);
        FileAssetEntity asset = mock(FileAssetEntity.class);
        when(asset.getMimeType()).thenReturn("text/plain");
        when(asset.getFileName()).thenReturn("cv-student.pdf");
        when(asset.getFileSizeBytes()).thenReturn(64L);
        when(cvRepository.findActiveByStudentId(studentId)).thenReturn(Optional.of(cv));
        when(fileAssetRepository.findById(assetId)).thenReturn(Optional.of(asset));

        assertThatThrownBy(() -> service.resolve(studentId)).isInstanceOf(CvFileUnavailableException.class);
    }

    @Test
    void checksumOrFileReadFailureMapsToStableUnavailableError() {
        UUID studentId = UUID.randomUUID();
        UUID assetId = UUID.randomUUID();
        CvEntity cv = activeCv(UUID.randomUUID(), studentId, assetId, 64);
        FileAssetEntity asset = mock(FileAssetEntity.class);
        when(asset.getMimeType()).thenReturn("application/pdf");
        when(asset.getFileName()).thenReturn("cv-student.pdf");
        when(asset.getFileSizeBytes()).thenReturn(64L);
        when(asset.getStorageKey()).thenReturn("cv/objects/2026/08/object.pdf");
        when(asset.getChecksumSha256()).thenReturn("b".repeat(64));
        when(cvRepository.findActiveByStudentId(studentId)).thenReturn(Optional.of(cv));
        when(fileAssetRepository.findById(assetId)).thenReturn(Optional.of(asset));
        when(integrityService.readVerified(asset.getStorageKey(), 64, "b".repeat(64)))
                .thenThrow(mock(CvFileIntegrityService.FileIntegrityException.class));

        assertThatThrownBy(() -> service.resolve(studentId)).isInstanceOf(CvFileUnavailableException.class);
    }

    private CvEntity activeCv(UUID cvId, UUID studentId, UUID assetId, long size) {
        CvEntity cv = new CvEntity();
        cv.setId(cvId);
        cv.setStudentId(studentId);
        cv.setRevision(3);
        cv.setPdfFileAssetId(assetId);
        cv.setPdfFileName("cv-student.pdf");
        cv.setPdfFileSizeBytes(size);
        cv.setSourceFingerprint("c".repeat(64));
        return cv;
    }
}
