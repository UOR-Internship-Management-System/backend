package lk.ac.ruhuna.dcs.cvmanagement.modules.cv.application;

import java.time.Clock;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.UUID;
import lk.ac.ruhuna.dcs.cvmanagement.modules.cv.api.dto.request.CvPreviewRequest;
import lk.ac.ruhuna.dcs.cvmanagement.modules.cv.api.dto.response.CvPreviewResponse;
import lk.ac.ruhuna.dcs.cvmanagement.modules.cv.domain.model.CvConfiguration;
import lk.ac.ruhuna.dcs.cvmanagement.modules.cv.persistence.entity.CvPreviewEntity;
import lk.ac.ruhuna.dcs.cvmanagement.modules.cv.persistence.repository.CvPreviewRepository;
import lk.ac.ruhuna.dcs.cvmanagement.modules.studentprofile.persistence.entity.StudentEntity;
import lk.ac.ruhuna.dcs.cvmanagement.modules.studentprofile.persistence.repository.StudentRepository;
import lk.ac.ruhuna.dcs.cvmanagement.shared.error.ForbiddenException;
import lk.ac.ruhuna.dcs.cvmanagement.shared.error.NotFoundException;
import lk.ac.ruhuna.dcs.cvmanagement.shared.security.CurrentActorProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Creates a durable, owner-scoped preview snapshot from authoritative Student source data. */
@Service
public class CvPreviewService {

    private final CurrentActorProvider currentActorProvider;
    private final StudentRepository studentRepository;
    private final CvSourceQueryService sourceQueryService;
    private final CvSourceFingerprintService fingerprintService;
    private final CvHtmlRenderer htmlRenderer;
    private final CvPreviewRepository previewRepository;
    private final CvPreviewSelectionStore selectionStore;
    private final CvFreshnessService freshnessService;
    private final Clock clock;
    private final Duration previewTtl;

    public CvPreviewService(
            CurrentActorProvider currentActorProvider,
            StudentRepository studentRepository,
            CvSourceQueryService sourceQueryService,
            CvSourceFingerprintService fingerprintService,
            CvHtmlRenderer htmlRenderer,
            CvPreviewRepository previewRepository,
            CvPreviewSelectionStore selectionStore,
            CvFreshnessService freshnessService,
            Clock clock,
            @Value("${app.cv.preview-ttl:PT15M}") Duration previewTtl) {
        this.currentActorProvider = currentActorProvider;
        this.studentRepository = studentRepository;
        this.sourceQueryService = sourceQueryService;
        this.fingerprintService = fingerprintService;
        this.htmlRenderer = htmlRenderer;
        this.previewRepository = previewRepository;
        this.selectionStore = selectionStore;
        this.freshnessService = freshnessService;
        this.clock = clock;
        this.previewTtl = previewTtl;
    }

    @Transactional
    public CvPreviewResponse createPreview(CvPreviewRequest request) {
        StudentEntity student = currentStudent();
        CvConfiguration configuration = CvConfiguration.from(request);
        var document = sourceQueryService.load(student, configuration);
        String sourceFingerprint = fingerprintService.fingerprint(document);
        String htmlPreview = htmlRenderer.render(document);

        OffsetDateTime generatedAt = OffsetDateTime.now(clock);
        OffsetDateTime expiresAt = generatedAt.plus(previewTtl);
        UUID previewId = UUID.randomUUID();

        CvPreviewEntity preview = new CvPreviewEntity();
        preview.setPreviewId(previewId);
        preview.setStudentId(student.getId());
        preview.setSourceFingerprint(sourceFingerprint);
        preview.setGeneratedAt(generatedAt);
        preview.setExpiresAt(expiresAt);
        preview.setCreatedAt(generatedAt);
        previewRepository.save(preview);
        selectionStore.save(previewId, student.getId(), configuration);

        return new CvPreviewResponse(
                previewId,
                htmlPreview,
                freshnessService.getFreshness(),
                configuration.toResponse(),
                generatedAt,
                expiresAt);
    }

    private StudentEntity currentStudent() {
        var actor = currentActorProvider.currentActor()
                .orElseThrow(() -> new ForbiddenException("No authenticated Student context."));
        return studentRepository.findByUserAccountId(actor.userId())
                .orElseThrow(() -> new NotFoundException("Student record not found for the authenticated account."));
    }
}
