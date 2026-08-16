package lk.ac.ruhuna.dcs.cvmanagement.modules.cv.application;

import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import lk.ac.ruhuna.dcs.cvmanagement.modules.cv.api.dto.response.CvPreviewConfigurationResponse;
import lk.ac.ruhuna.dcs.cvmanagement.modules.cv.api.dto.response.CvResponse;
import lk.ac.ruhuna.dcs.cvmanagement.modules.cv.api.dto.response.GeneratedFileMetadataResponse;
import lk.ac.ruhuna.dcs.cvmanagement.modules.cv.domain.CvPreviewCache;
import lk.ac.ruhuna.dcs.cvmanagement.modules.cv.persistence.entity.CvEntity;
import lk.ac.ruhuna.dcs.cvmanagement.modules.cv.persistence.repository.CvRepository;
import lk.ac.ruhuna.dcs.cvmanagement.modules.studentprofile.persistence.entity.StudentEntity;
import lk.ac.ruhuna.dcs.cvmanagement.modules.studentprofile.persistence.repository.StudentRepository;
import lk.ac.ruhuna.dcs.cvmanagement.shared.error.ConflictException;
import lk.ac.ruhuna.dcs.cvmanagement.shared.error.ForbiddenException;
import lk.ac.ruhuna.dcs.cvmanagement.shared.error.NotFoundException;
import lk.ac.ruhuna.dcs.cvmanagement.shared.error.PreconditionFailedException;
import lk.ac.ruhuna.dcs.cvmanagement.shared.security.CurrentActorProvider;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CvSaveService {

    private final CurrentActorProvider currentActorProvider;
    private final StudentRepository studentRepository;
    private final CvRepository cvRepository;
    private final CvPreviewCache previewCache;
    private final CvFreshnessService freshnessService;

    public CvSaveService(
        CurrentActorProvider currentActorProvider,
        StudentRepository studentRepository,
        CvRepository cvRepository,
        CvPreviewCache previewCache,
        CvFreshnessService freshnessService) {
        this.currentActorProvider = currentActorProvider;
        this.studentRepository = studentRepository;
        this.cvRepository = cvRepository;
        this.previewCache = previewCache;
        this.freshnessService = freshnessService;
    }

    @Transactional(readOnly = true)
    public CvResponse getCurrent() {
        UUID studentId = currentStudentId();
        CvEntity cv = cvRepository.findByStudentId(studentId)
            .orElseThrow(() -> new NotFoundException("No saved CV exists yet."));
        return toResponse(cv);
    }

    @Transactional
    public CvResponse save(UUID previewId, Long ifMatchRevision, boolean ifNoneMatchStar) {
        UUID studentId = currentStudentId();

        CvPreviewCache.CachedPreview preview = previewCache.get(previewId);
        if (preview == null || !preview.studentId().equals(studentId)) {
            throw new NotFoundException("CV preview not found or has expired. Generate a new preview first.");
        }

        var existing = cvRepository.findByStudentId(studentId);

        if (existing.isEmpty()) {
            if (!ifNoneMatchStar) {
                throw new PreconditionFailedException(
                    "No saved CV exists yet. Use If-None-Match: * to create the first one.");
            }
        } else {
            if (ifNoneMatchStar) {
                throw new ConflictException("A CV already exists for this Student. Use If-Match instead.");
            }
            if (ifMatchRevision == null || existing.get().getRevision() != ifMatchRevision) {
                throw new PreconditionFailedException("CV has been modified since it was last read.");
            }
        }

        CvEntity cv = existing.orElseGet(() -> {
            CvEntity fresh = new CvEntity();
            fresh.setId(UUID.randomUUID());
            fresh.setStudentId(studentId);
            fresh.setRevision(0);
            fresh.setCreatedAt(OffsetDateTime.now());
            return fresh;
        });

        OffsetDateTime now = OffsetDateTime.now();
        cv.setRevision(cv.getRevision() + 1);
        cv.setGeneratedAt(preview.generatedAt());
        cv.setSavedAt(now);
        cv.setIncludedExperienceIds(join(preview.includedExperienceIds()));
        cv.setIncludedProjectIds(join(preview.includedProjectIds()));
        cv.setIncludedCertificateIds(join(preview.includedCertificateIds()));
        cv.setIncludedAwardIds(join(preview.includedAwardIds()));
        cv.setIncludedActivityIds(join(preview.includedActivityIds()));

        // Placeholder metadata until real LaTeX/PDF generation lands in Part 4.
        cv.setPdfFileName("cv-" + studentId + "-r" + cv.getRevision() + ".pdf");
        cv.setPdfFileSizeBytes(0);

        CvEntity saved = cvRepository.save(cv);
        previewCache.remove(previewId);
        return toResponse(saved);
    }

    private CvResponse toResponse(CvEntity cv) {
        String freshnessStatus = freshnessService.getFreshness().status();
        // A freshly-saved CV is always CURRENT relative to itself; only compute OUTDATED
        // via the real freshness check on subsequent reads.
        String effectiveStatus = "NOT_SAVED".equals(freshnessStatus) ? "CURRENT" : freshnessStatus;

        var configuration = new CvPreviewConfigurationResponse(
            split(cv.getIncludedExperienceIds()),
            split(cv.getIncludedProjectIds()),
            split(cv.getIncludedCertificateIds()),
            split(cv.getIncludedAwardIds()),
            split(cv.getIncludedActivityIds()));

        var pdfFile = new GeneratedFileMetadataResponse(
            cv.getPdfFileName(), "application/pdf", cv.getPdfFileSizeBytes(), cv.getGeneratedAt());

        return new CvResponse(
            cv.getId(),
            cv.getRevision(),
            cv.getCreatedAt(),
            cv.getGeneratedAt(),
            cv.getSavedAt(),
            "/api/v1/me/cv/download",
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
