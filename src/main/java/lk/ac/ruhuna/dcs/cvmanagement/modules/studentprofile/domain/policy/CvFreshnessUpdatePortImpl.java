package lk.ac.ruhuna.dcs.cvmanagement.modules.studentprofile.domain.policy;

import java.time.OffsetDateTime;
import java.util.UUID;
import lk.ac.ruhuna.dcs.cvmanagement.modules.cv.persistence.entity.CvSourceFreshnessEntity;
import lk.ac.ruhuna.dcs.cvmanagement.modules.cv.persistence.repository.CvSourceFreshnessRepository;
import org.springframework.stereotype.Component;

@Component
public class CvFreshnessUpdatePortImpl implements CvFreshnessUpdatePort {

    private final CvSourceFreshnessRepository repository;

    public CvFreshnessUpdatePortImpl(CvSourceFreshnessRepository repository) {
        this.repository = repository;
    }

    @Override
    public void markStale(UUID studentId, String sourceModule) {
        CvSourceFreshnessEntity row = repository.findById(studentId)
            .orElseGet(() -> {
                CvSourceFreshnessEntity fresh = new CvSourceFreshnessEntity();
                fresh.setStudentId(studentId);
                return fresh;
            });

        OffsetDateTime now = OffsetDateTime.now();
        switch (sourceModule) {
            case "studentprofile" -> row.setProfileChangedAt(now);
            case "skills" -> row.setDeclaredSkillsChangedAt(now);
            case "projects" -> row.setProjectsChangedAt(now);
            case "academics" -> row.setAcademicRecordsChangedAt(now);
            default -> { /* unknown source, ignore */ }
        }
        repository.save(row);
    }
}
