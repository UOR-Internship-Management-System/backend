package lk.ac.ruhuna.dcs.cvmanagement.modules.cv.application;

import java.time.Clock;
import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import lk.ac.ruhuna.dcs.cvmanagement.modules.cv.api.dto.response.CvPreviewConfigurationResponse;
import lk.ac.ruhuna.dcs.cvmanagement.modules.cv.api.dto.response.CvResponse;
import lk.ac.ruhuna.dcs.cvmanagement.modules.cv.api.dto.response.GeneratedFileMetadataResponse;
import lk.ac.ruhuna.dcs.cvmanagement.modules.cv.domain.exception.CvNotSavedException;
import lk.ac.ruhuna.dcs.cvmanagement.modules.cv.domain.exception.CvPreconditionRequiredException;
import lk.ac.ruhuna.dcs.cvmanagement.modules.cv.domain.exception.CvPreviewExpiredException;
import lk.ac.ruhuna.dcs.cvmanagement.modules.cv.domain.exception.StaleCvException;
import lk.ac.ruhuna.dcs.cvmanagement.modules.cv.domain.model.CvConfiguration;
import lk.ac.ruhuna.dcs.cvmanagement.modules.cv.persistence.entity.CvEntity;
import lk.ac.ruhuna.dcs.cvmanagement.modules.cv.persistence.entity.CvPreviewEntity;
import lk.ac.ruhuna.dcs.cvmanagement.modules.cv.persistence.repository.CvPreviewRepository;
import lk.ac.ruhuna.dcs.cvmanagement.modules.cv.persistence.repository.CvRepository;
import lk.ac.ruhuna.dcs.cvmanagement.modules.studentprofile.persistence.entity.StudentEntity;
import lk.ac.ruhuna.dcs.cvmanagement.modules.studentprofile.persistence.repository.StudentRepository;
import lk.ac.ruhuna.dcs.cvmanagement.shared.error.ForbiddenException;
import lk.ac.ruhuna.dcs.cvmanagement.shared.error.NotFoundException;
import lk.ac.ruhuna.dcs.cvmanagement.shared.security.CurrentActorProvider;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Interim active-CV persistence over the durable preview model.
 *
 * <p>Batch 1 removes the JVM preview cache and makes preview consumption/idempotency durable. Real
 * staged PDF promotion, source-fingerprint revalidation, normalized active-selection writes, locking,
 * and final conditional HTTP semantics are completed by Patches 4-5 as defined by the implementation plan.</p>
 */
@Service
public class CvSaveService {

    private final CurrentActorProvider currentActorProvider;
    private final StudentRepository studentRepository;
    private final CvRepository cvRepository;
    private final CvPreviewRepository previewRepository;
    private final CvPreviewSelectionStore previewSelectionStore;
    private final CvFreshnessService freshnessService;
    private final Clock clock;

    public CvSaveService(
            CurrentActorProvider currentActorProvider,
            StudentRepository studentRepository,
            CvRepository cvRepository,
            CvPreviewRepository previewRepository,
            CvPreviewSelectionStore previewSelectionStore,
            CvFreshnessService freshnessService,
            Clock clock) {
        this.currentActorProvider = currentActorProvider;
        this.studentRepository = studentRepository;
        this.cvRepository = cvRepository;
        this.previewRepository = previewRepository;
        this.previewSelectionStore = previewSelectionStore;
        this.freshnessService = freshnessService;
        this.clock = clock;
    }

    @Transactional(readOnly = true)
    public CvResponse getCurrent() {
        UUID studentId = currentStudentId();
        CvEntity cv = cvRepository.findByStudentId(studentId)
                .orElseThrow(CvNotSavedException::new);
        return toResponse(cv);
    }

    @Transactional
    public CvResponse save(UUID previewId, Long ifMatchRevision, boolean ifNoneMatchStar) {
        UUID studentId = currentStudentId();
        OffsetDateTime now = OffsetDateTime.now(clock);

        CvPreviewEntity preview = previewRepository.findByPreviewIdAndStudentId(previewId, studentId)
                .orElseThrow(CvPreviewExpiredException::new);
        var existing = cvRepository.findByStudentId(studentId);

        if (preview.getConsumedAt() != null) {
            if (existing.isPresent()
                    && existing.get().getId().equals(preview.getResultCvId())
                    && existing.get().getRevision() == preview.getResultRevision()) {
                return toResponse(existing.get());
            }
            throw new CvPreviewExpiredException();
        }
        if (!preview.getExpiresAt().isAfter(now)) {
            throw new CvPreviewExpiredException();
        }

        if (existing.isEmpty()) {
            if (!ifNoneMatchStar) {
                throw new CvPreconditionRequiredException();
            }
        } else {
            if (ifNoneMatchStar) {
                throw new StaleCvException();
            }
            if (ifMatchRevision == null) {
                throw new CvPreconditionRequiredException();
            }
            if (existing.get().getRevision() != ifMatchRevision) {
                throw new StaleCvException();
            }
        }

        CvConfiguration configuration = previewSelectionStore.load(previewId);
        CvEntity cv = existing.orElseGet(() -> {
            CvEntity fresh = new CvEntity();
            fresh.setId(UUID.randomUUID());
            fresh.setStudentId(studentId);
            fresh.setRevision(0);
            fresh.setCreatedAt(now);
            return fresh;
        });

        cv.setRevision(cv.getRevision() + 1);
        cv.setGeneratedAt(preview.getGeneratedAt());
        cv.setSavedAt(now);
        cv.setUpdatedAt(now);
        cv.setSourceFingerprint(preview.getSourceFingerprint());
        cv.setLastSavedPreviewId(previewId);
        cv.setIncludedExperienceIds(join(configuration.includedExperienceIds()));
        cv.setIncludedProjectIds(join(configuration.includedProjectIds()));
        cv.setIncludedCertificateIds(join(configuration.includedCertificateIds()));
        cv.setIncludedAwardIds(join(configuration.includedAwardIds()));
        cv.setIncludedActivityIds(join(configuration.includedActivityIds()));

        // Transitional metadata only. Patch 4 replaces this with the staged, non-zero PDF artifact.
        cv.setPdfFileName("cv-" + studentId + "-r" + cv.getRevision() + ".pdf");
        cv.setPdfFileSizeBytes(0L);

        CvEntity saved = cvRepository.save(cv);
        preview.setConsumedAt(now);
        preview.setResultCvId(saved.getId());
        preview.setResultRevision(saved.getRevision());
        previewRepository.save(preview);
        return toResponse(saved);
    }

    private CvResponse toResponse(CvEntity cv) {
        String freshnessStatus = freshnessService.getFreshness().status();
        String effectiveStatus = "NOT_SAVED".equals(freshnessStatus) ? "CURRENT" : freshnessStatus;

        var configuration = new CvPreviewConfigurationResponse(
                split(cv.getIncludedExperienceIds()),
                split(cv.getIncludedProjectIds()),
                split(cv.getIncludedCertificateIds()),
                split(cv.getIncludedAwardIds()),
                split(cv.getIncludedActivityIds()));

        long fileSize = cv.getPdfFileSizeBytes() == null ? 0L : cv.getPdfFileSizeBytes();
        var pdfFile = new GeneratedFileMetadataResponse(
                cv.getPdfFileName(), "application/pdf", fileSize, cv.getGeneratedAt());

        return new CvResponse(
                cv.getId(),
                cv.getRevision(),
                cv.getCreatedAt(),
                cv.getGeneratedAt(),
                cv.getSavedAt(),
                "/me/cv/download",
                effectiveStatus,
                configuration,
                pdfFile);
    }

    private String join(List<UUID> ids) {
        return ids == null || ids.isEmpty() ? "" : ids.stream().map(UUID::toString).collect(Collectors.joining(","));
    }

    private List<UUID> split(String stored) {
        if (stored == null || stored.isBlank()) return List.of();
        return Arrays.stream(stored.split(",")).map(UUID::fromString).toList();
    }

    private UUID currentStudentId() {
        var actor = currentActorProvider.currentActor()
                .orElseThrow(() -> new ForbiddenException("No authenticated Student context."));
        StudentEntity student = studentRepository.findByUserAccountId(actor.userId())
                .orElseThrow(() -> new NotFoundException("Student record not found for the authenticated account."));
        return student.getId();
    }
}
