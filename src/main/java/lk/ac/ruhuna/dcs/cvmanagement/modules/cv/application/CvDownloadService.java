package lk.ac.ruhuna.dcs.cvmanagement.modules.cv.application;

import java.util.Map;
import lk.ac.ruhuna.dcs.cvmanagement.modules.cv.application.port.ActiveCvFileResolver;
import lk.ac.ruhuna.dcs.cvmanagement.modules.studentprofile.persistence.repository.StudentRepository;
import lk.ac.ruhuna.dcs.cvmanagement.shared.audit.AuditEventCategory;
import lk.ac.ruhuna.dcs.cvmanagement.shared.audit.AuditEventPublisher;
import lk.ac.ruhuna.dcs.cvmanagement.shared.error.ForbiddenException;
import lk.ac.ruhuna.dcs.cvmanagement.shared.error.NotFoundException;
import lk.ac.ruhuna.dcs.cvmanagement.shared.security.CurrentActorProvider;
import org.springframework.stereotype.Service;

/** Owner-scoped Student download application service. */
@Service
public class CvDownloadService {
    private final CurrentActorProvider currentActorProvider;
    private final StudentRepository studentRepository;
    private final ActiveCvFileResolver fileResolver;
    private final AuditEventPublisher auditEventPublisher;

    public CvDownloadService(
            CurrentActorProvider currentActorProvider,
            StudentRepository studentRepository,
            ActiveCvFileResolver fileResolver,
            AuditEventPublisher auditEventPublisher) {
        this.currentActorProvider = currentActorProvider;
        this.studentRepository = studentRepository;
        this.fileResolver = fileResolver;
        this.auditEventPublisher = auditEventPublisher;
    }

    public ActiveCvFileResolver.ResolvedCvFile downloadCurrent() {
        var actor = currentActorProvider.currentActor()
                .orElseThrow(() -> new ForbiddenException("No authenticated Student context."));
        var student = studentRepository.findByUserAccountId(actor.userId())
                .orElseThrow(() -> new NotFoundException("Student record not found for the authenticated account."));
        var file = fileResolver.resolve(student.getId());
        auditEventPublisher.recordRequired(
                actor.userId(), "STUDENT", "CV_DOWNLOADED_BY_STUDENT", AuditEventCategory.CV_MANAGEMENT,
                "CV", file.cvId().toString(), Map.of("revision", file.revision(), "fileSizeBytes", file.fileSizeBytes()));
        return file;
    }
}
