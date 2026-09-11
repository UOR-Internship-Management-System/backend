package lk.ac.ruhuna.dcs.cvmanagement.modules.cv.application;

import java.util.List;
import java.util.UUID;
import lk.ac.ruhuna.dcs.cvmanagement.modules.cv.domain.model.CvConfiguration;
import lk.ac.ruhuna.dcs.cvmanagement.modules.cv.persistence.entity.AbstractCvActiveSelectionEntity;
import lk.ac.ruhuna.dcs.cvmanagement.modules.cv.persistence.entity.CvActiveSelectionId;
import lk.ac.ruhuna.dcs.cvmanagement.modules.cv.persistence.entity.CvSelectedActivityEntity;
import lk.ac.ruhuna.dcs.cvmanagement.modules.cv.persistence.entity.CvSelectedAwardEntity;
import lk.ac.ruhuna.dcs.cvmanagement.modules.cv.persistence.entity.CvSelectedCertificateEntity;
import lk.ac.ruhuna.dcs.cvmanagement.modules.cv.persistence.entity.CvSelectedExperienceEntity;
import lk.ac.ruhuna.dcs.cvmanagement.modules.cv.persistence.entity.CvSelectedProjectEntity;
import lk.ac.ruhuna.dcs.cvmanagement.modules.cv.persistence.repository.CvSelectedActivityRepository;
import lk.ac.ruhuna.dcs.cvmanagement.modules.cv.persistence.repository.CvSelectedAwardRepository;
import lk.ac.ruhuna.dcs.cvmanagement.modules.cv.persistence.repository.CvSelectedCertificateRepository;
import lk.ac.ruhuna.dcs.cvmanagement.modules.cv.persistence.repository.CvSelectedExperienceRepository;
import lk.ac.ruhuna.dcs.cvmanagement.modules.cv.persistence.repository.CvSelectedProjectRepository;
import org.springframework.stereotype.Component;

/** Owns the normalized record-level selection snapshot attached to the single active CV row. */
@Component
public class CvActiveSelectionStore {
    private final CvSelectedExperienceRepository experiences;
    private final CvSelectedProjectRepository projects;
    private final CvSelectedCertificateRepository certificates;
    private final CvSelectedAwardRepository awards;
    private final CvSelectedActivityRepository activities;

    public CvActiveSelectionStore(
            CvSelectedExperienceRepository experiences,
            CvSelectedProjectRepository projects,
            CvSelectedCertificateRepository certificates,
            CvSelectedAwardRepository awards,
            CvSelectedActivityRepository activities) {
        this.experiences = experiences;
        this.projects = projects;
        this.certificates = certificates;
        this.awards = awards;
        this.activities = activities;
    }

    public void replace(UUID cvId, UUID studentId, CvConfiguration configuration) {
        experiences.deleteAllByIdCvId(cvId); experiences.flush();
        projects.deleteAllByIdCvId(cvId); projects.flush();
        certificates.deleteAllByIdCvId(cvId); certificates.flush();
        awards.deleteAllByIdCvId(cvId); awards.flush();
        activities.deleteAllByIdCvId(cvId); activities.flush();

        experiences.saveAll(configuration.includedExperienceIds().stream()
                .map(id -> new CvSelectedExperienceEntity(cvId, studentId, id)).toList());
        projects.saveAll(configuration.includedProjectIds().stream()
                .map(id -> new CvSelectedProjectEntity(cvId, studentId, id)).toList());
        certificates.saveAll(configuration.includedCertificateIds().stream()
                .map(id -> new CvSelectedCertificateEntity(cvId, studentId, id)).toList());
        awards.saveAll(configuration.includedAwardIds().stream()
                .map(id -> new CvSelectedAwardEntity(cvId, studentId, id)).toList());
        activities.saveAll(configuration.includedActivityIds().stream()
                .map(id -> new CvSelectedActivityEntity(cvId, studentId, id)).toList());
    }

    public CvConfiguration load(UUID cvId) {
        return new CvConfiguration(
                sourceIds(experiences.findAllByIdCvIdOrderByIdSourceRecordIdAsc(cvId)),
                sourceIds(projects.findAllByIdCvIdOrderByIdSourceRecordIdAsc(cvId)),
                sourceIds(certificates.findAllByIdCvIdOrderByIdSourceRecordIdAsc(cvId)),
                sourceIds(awards.findAllByIdCvIdOrderByIdSourceRecordIdAsc(cvId)),
                sourceIds(activities.findAllByIdCvIdOrderByIdSourceRecordIdAsc(cvId)));
    }

    private List<UUID> sourceIds(List<? extends AbstractCvActiveSelectionEntity> rows) {
        return rows.stream().map(AbstractCvActiveSelectionEntity::getId).map(CvActiveSelectionId::getSourceRecordId).toList();
    }
}
