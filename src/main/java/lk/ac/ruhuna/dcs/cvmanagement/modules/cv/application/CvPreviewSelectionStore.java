package lk.ac.ruhuna.dcs.cvmanagement.modules.cv.application;

import java.util.List;
import java.util.UUID;
import lk.ac.ruhuna.dcs.cvmanagement.modules.cv.domain.model.CvConfiguration;
import lk.ac.ruhuna.dcs.cvmanagement.modules.cv.persistence.entity.CvPreviewActivityEntity;
import lk.ac.ruhuna.dcs.cvmanagement.modules.cv.persistence.entity.CvPreviewAwardEntity;
import lk.ac.ruhuna.dcs.cvmanagement.modules.cv.persistence.entity.CvPreviewCertificateEntity;
import lk.ac.ruhuna.dcs.cvmanagement.modules.cv.persistence.entity.CvPreviewExperienceEntity;
import lk.ac.ruhuna.dcs.cvmanagement.modules.cv.persistence.entity.CvPreviewProjectEntity;
import lk.ac.ruhuna.dcs.cvmanagement.modules.cv.persistence.entity.CvPreviewSelectionId;
import lk.ac.ruhuna.dcs.cvmanagement.modules.cv.persistence.repository.CvPreviewActivityRepository;
import lk.ac.ruhuna.dcs.cvmanagement.modules.cv.persistence.repository.CvPreviewAwardRepository;
import lk.ac.ruhuna.dcs.cvmanagement.modules.cv.persistence.repository.CvPreviewCertificateRepository;
import lk.ac.ruhuna.dcs.cvmanagement.modules.cv.persistence.repository.CvPreviewExperienceRepository;
import lk.ac.ruhuna.dcs.cvmanagement.modules.cv.persistence.repository.CvPreviewProjectRepository;
import org.springframework.stereotype.Component;

/** Persists and reconstructs the immutable record-level configuration attached to a durable preview. */
@Component
public class CvPreviewSelectionStore {

    private final CvPreviewExperienceRepository experienceRepository;
    private final CvPreviewProjectRepository projectRepository;
    private final CvPreviewCertificateRepository certificateRepository;
    private final CvPreviewAwardRepository awardRepository;
    private final CvPreviewActivityRepository activityRepository;

    public CvPreviewSelectionStore(
            CvPreviewExperienceRepository experienceRepository,
            CvPreviewProjectRepository projectRepository,
            CvPreviewCertificateRepository certificateRepository,
            CvPreviewAwardRepository awardRepository,
            CvPreviewActivityRepository activityRepository) {
        this.experienceRepository = experienceRepository;
        this.projectRepository = projectRepository;
        this.certificateRepository = certificateRepository;
        this.awardRepository = awardRepository;
        this.activityRepository = activityRepository;
    }

    public void save(UUID previewId, UUID studentId, CvConfiguration configuration) {
        experienceRepository.saveAll(configuration.includedExperienceIds().stream()
                .map(id -> new CvPreviewExperienceEntity(previewId, studentId, id)).toList());
        projectRepository.saveAll(configuration.includedProjectIds().stream()
                .map(id -> new CvPreviewProjectEntity(previewId, studentId, id)).toList());
        certificateRepository.saveAll(configuration.includedCertificateIds().stream()
                .map(id -> new CvPreviewCertificateEntity(previewId, studentId, id)).toList());
        awardRepository.saveAll(configuration.includedAwardIds().stream()
                .map(id -> new CvPreviewAwardEntity(previewId, studentId, id)).toList());
        activityRepository.saveAll(configuration.includedActivityIds().stream()
                .map(id -> new CvPreviewActivityEntity(previewId, studentId, id)).toList());
    }

    public CvConfiguration load(UUID previewId) {
        return new CvConfiguration(
                sourceIds(experienceRepository.findAllByIdPreviewIdOrderByIdSourceRecordIdAsc(previewId)),
                sourceIds(projectRepository.findAllByIdPreviewIdOrderByIdSourceRecordIdAsc(previewId)),
                sourceIds(certificateRepository.findAllByIdPreviewIdOrderByIdSourceRecordIdAsc(previewId)),
                sourceIds(awardRepository.findAllByIdPreviewIdOrderByIdSourceRecordIdAsc(previewId)),
                sourceIds(activityRepository.findAllByIdPreviewIdOrderByIdSourceRecordIdAsc(previewId)));
    }

    private List<UUID> sourceIds(List<? extends lk.ac.ruhuna.dcs.cvmanagement.modules.cv.persistence.entity.AbstractCvPreviewSelectionEntity> rows) {
        return rows.stream().map(row -> row.getId()).map(CvPreviewSelectionId::getSourceRecordId).toList();
    }
}
