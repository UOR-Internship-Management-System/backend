package lk.ac.ruhuna.dcs.cvmanagement.modules.cv.application;

import java.time.Clock;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lk.ac.ruhuna.dcs.cvmanagement.modules.cv.api.dto.response.CvFreshnessResponse;
import lk.ac.ruhuna.dcs.cvmanagement.modules.cv.persistence.entity.CvEntity;
import lk.ac.ruhuna.dcs.cvmanagement.modules.cv.persistence.entity.CvSourceFreshnessEntity;
import lk.ac.ruhuna.dcs.cvmanagement.modules.cv.persistence.repository.CvRepository;
import lk.ac.ruhuna.dcs.cvmanagement.modules.cv.persistence.repository.CvSourceFreshnessRepository;
import lk.ac.ruhuna.dcs.cvmanagement.modules.studentprofile.persistence.repository.StudentRepository;
import lk.ac.ruhuna.dcs.cvmanagement.shared.error.ForbiddenException;
import lk.ac.ruhuna.dcs.cvmanagement.shared.error.NotFoundException;
import lk.ac.ruhuna.dcs.cvmanagement.shared.security.CurrentActorProvider;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class CvFreshnessService {

    private final CurrentActorProvider currentActorProvider;
    private final StudentRepository studentRepository;
    private final CvRepository cvRepository;
    private final CvSourceFreshnessRepository freshnessRepository;
    private final Clock clock;

    public CvFreshnessService(
        CurrentActorProvider currentActorProvider,
        StudentRepository studentRepository,
        CvRepository cvRepository,
        CvSourceFreshnessRepository freshnessRepository,
        Clock clock) {
        this.currentActorProvider = currentActorProvider;
        this.studentRepository = studentRepository;
        this.cvRepository = cvRepository;
        this.freshnessRepository = freshnessRepository;
        this.clock = clock;
    }

    UUID currentStudentId() {
        var actor = currentActorProvider.currentActor()
            .orElseThrow(() -> new ForbiddenException("No authenticated Student context."));
        return studentRepository.findByUserAccountId(actor.userId())
            .orElseThrow(() -> new NotFoundException("Student record not found for the authenticated account."))
            .getId();
    }

    public CvFreshnessResponse getFreshness() {
        UUID studentId = currentStudentId();
        OffsetDateTime now = OffsetDateTime.ofInstant(clock.instant(), ZoneOffset.UTC);
        var savedCv = cvRepository.findByStudentId(studentId);

        if (savedCv.isEmpty()) {
            return new CvFreshnessResponse(
                "NOT_SAVED", List.of(), null, null, now, "No saved CV exists yet. Generate a preview to get started.");
        }

        CvEntity cv = savedCv.get();
        List<String> changedAreas = computeChangedAreas(studentId, cv.getSavedAt());

        if (changedAreas.isEmpty()) {
            return new CvFreshnessResponse(
                "CURRENT", List.of(), cv.getId(), cv.getSavedAt(), now, "Your saved CV reflects your latest information.");
        }
        return new CvFreshnessResponse(
            "OUTDATED", changedAreas, cv.getId(), cv.getSavedAt(), now,
            "Some of your information has changed since this CV was saved.");
    }

    private List<String> computeChangedAreas(UUID studentId, OffsetDateTime savedAt) {
        List<String> areas = new ArrayList<>();
        var freshness = freshnessRepository.findById(studentId);
        if (freshness.isEmpty()) {
            return areas;
        }
        CvSourceFreshnessEntity row = freshness.get();
        if (isAfter(row.getProfileChangedAt(), savedAt)) areas.add("PROFILE");
        if (isAfter(row.getDeclaredSkillsChangedAt(), savedAt)) areas.add("DECLARED_SKILLS");
        if (isAfter(row.getProjectsChangedAt(), savedAt)) areas.add("PROJECTS");
        if (isAfter(row.getAcademicRecordsChangedAt(), savedAt)) areas.add("ACADEMIC_RECORDS");
        return areas;
    }

    private boolean isAfter(OffsetDateTime changedAt, OffsetDateTime savedAt) {
        return changedAt != null && changedAt.isAfter(savedAt);
    }
}
