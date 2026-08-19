package lk.ac.ruhuna.dcs.cvmanagement.modules.cv.application;

import java.util.UUID;
import lk.ac.ruhuna.dcs.cvmanagement.infrastructure.storage.FileAssetRepository;
import lk.ac.ruhuna.dcs.cvmanagement.modules.cv.application.port.ActiveCvFileResolver;
import lk.ac.ruhuna.dcs.cvmanagement.modules.cv.domain.exception.CvFileUnavailableException;
import lk.ac.ruhuna.dcs.cvmanagement.modules.cv.domain.exception.CvNotSavedException;
import lk.ac.ruhuna.dcs.cvmanagement.modules.cv.persistence.repository.CvRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Resolves and integrity-checks the exact durable PDF referenced by the active CV row. */
@Service
public class ActiveCvFileResolverService implements ActiveCvFileResolver {
    private final CvRepository cvRepository;
    private final FileAssetRepository fileAssetRepository;
    private final CvFileIntegrityService fileIntegrityService;

    public ActiveCvFileResolverService(
            CvRepository cvRepository,
            FileAssetRepository fileAssetRepository,
            CvFileIntegrityService fileIntegrityService) {
        this.cvRepository = cvRepository;
        this.fileAssetRepository = fileAssetRepository;
        this.fileIntegrityService = fileIntegrityService;
    }

    @Override
    @Transactional(readOnly = true)
    public ResolvedCvFile resolve(UUID studentId) {
        var cv = cvRepository.findActiveByStudentId(studentId).orElseThrow(CvNotSavedException::new);
        var asset = fileAssetRepository.findById(cv.getPdfFileAssetId()).orElseThrow(CvFileUnavailableException::new);
        if (!"application/pdf".equals(asset.getMimeType())
                || asset.getFileSizeBytes() != cv.getPdfFileSizeBytes()
                || !asset.getFileName().equals(cv.getPdfFileName())) {
            throw new CvFileUnavailableException();
        }
        try {
            byte[] bytes = fileIntegrityService.readVerified(
                    asset.getStorageKey(), asset.getFileSizeBytes(), asset.getChecksumSha256());
            return new ResolvedCvFile(cv.getId(), cv.getRevision(), asset.getFileName(), asset.getFileSizeBytes(), bytes);
        } catch (CvFileIntegrityService.FileIntegrityException exception) {
            throw new CvFileUnavailableException();
        }
    }
}
