package lk.ac.ruhuna.dcs.cvmanagement.modules.cv.application;

import lk.ac.ruhuna.dcs.cvmanagement.modules.cv.domain.model.CvConfiguration;
import lk.ac.ruhuna.dcs.cvmanagement.modules.cv.persistence.entity.CvPreviewEntity;
import lk.ac.ruhuna.dcs.cvmanagement.modules.cv.persistence.repository.CvPreviewRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Commits preview metadata and its immutable record-selection snapshot in one database transaction. */
@Service
public class CvPreviewPersistenceService {
    private final CvPreviewRepository previewRepository;
    private final CvPreviewSelectionStore selectionStore;

    public CvPreviewPersistenceService(CvPreviewRepository previewRepository, CvPreviewSelectionStore selectionStore) {
        this.previewRepository = previewRepository;
        this.selectionStore = selectionStore;
    }

    @Transactional
    public void persist(CvPreviewEntity preview, CvConfiguration configuration) {
        previewRepository.save(preview);
        selectionStore.save(preview.getPreviewId(), preview.getStudentId(), configuration);
    }
}
