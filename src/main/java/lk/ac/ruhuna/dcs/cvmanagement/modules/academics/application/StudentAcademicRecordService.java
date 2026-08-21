package lk.ac.ruhuna.dcs.cvmanagement.modules.academics.application;

import java.util.UUID;
import lk.ac.ruhuna.dcs.cvmanagement.modules.academics.api.dto.response.AcademicRecordResponse;
import lk.ac.ruhuna.dcs.cvmanagement.modules.academics.api.dto.response.AcademicRecordSourceResponse;
import lk.ac.ruhuna.dcs.cvmanagement.modules.academics.api.dto.response.GpaSummaryResponse;
import lk.ac.ruhuna.dcs.cvmanagement.modules.academics.api.error.AcademicLedgerErrors;
import lk.ac.ruhuna.dcs.cvmanagement.modules.academics.domain.AcademicRecordSort;
import lk.ac.ruhuna.dcs.cvmanagement.modules.academics.persistence.AcademicRecordQueryRepository;
import lk.ac.ruhuna.dcs.cvmanagement.modules.academics.persistence.entity.AcademicLedgerUploadEntity;
import lk.ac.ruhuna.dcs.cvmanagement.modules.academics.persistence.entity.StudentAcademicSummaryEntity;
import lk.ac.ruhuna.dcs.cvmanagement.modules.academics.persistence.repository.AcademicLedgerUploadRepository;
import lk.ac.ruhuna.dcs.cvmanagement.modules.academics.persistence.repository.StudentAcademicSummaryRepository;
import lk.ac.ruhuna.dcs.cvmanagement.modules.studentprofile.persistence.repository.StudentRepository;
import lk.ac.ruhuna.dcs.cvmanagement.shared.pagination.dto.PagedResponse;
import lk.ac.ruhuna.dcs.cvmanagement.shared.security.CurrentActor;
import lk.ac.ruhuna.dcs.cvmanagement.shared.security.CurrentActorProvider;
import lk.ac.ruhuna.dcs.cvmanagement.shared.security.RoleName;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Application service for a Student's own read-only committed academic history and GPA. */
@Service
public class StudentAcademicRecordService {

    private final AcademicRecordQueryRepository queryRepository;
    private final StudentAcademicSummaryRepository summaryRepository;
    private final AcademicLedgerUploadRepository uploadRepository;
    private final StudentRepository studentRepository;
    private final CurrentActorProvider currentActorProvider;

    public StudentAcademicRecordService(
        AcademicRecordQueryRepository queryRepository,
        StudentAcademicSummaryRepository summaryRepository,
        AcademicLedgerUploadRepository uploadRepository,
        StudentRepository studentRepository,
        CurrentActorProvider currentActorProvider) {
        this.queryRepository = queryRepository;
        this.summaryRepository = summaryRepository;
        this.uploadRepository = uploadRepository;
        this.studentRepository = studentRepository;
        this.currentActorProvider = currentActorProvider;
    }

    @Transactional(readOnly = true)
    public PagedResponse<AcademicRecordResponse> list(Integer page, Integer size, String sort, String search) {
        UUID studentId = currentStudentId();
        int safePage = validatePage(page);
        int safeSize = validateSize(size);
        AcademicRecordSort safeSort = AcademicRecordSort.fromApiValue(sort);
        String safeSearch = validateSearch(search);
        try {
            return PagedResponse.of(
                queryRepository.search(safeSearch, null, studentId, safePage, safeSize, safeSort),
                safeSort.apiValue());
        } catch (DataAccessException exception) {
            throw AcademicLedgerErrors.academicDataUnavailable(exception);
        }
    }

    @Transactional(readOnly = true)
    public GpaSummaryResponse getGpa() {
        UUID studentId = currentStudentId();
        return summaryRepository.findById(studentId)
            .map(this::toAvailableResponse)
            .orElseGet(() -> new GpaSummaryResponse(studentId, "NOT_AVAILABLE", null, null, null, null));
    }

    private GpaSummaryResponse toAvailableResponse(StudentAcademicSummaryEntity summary) {
        AcademicLedgerUploadEntity upload = uploadRepository.findById(summary.getSourceUploadId())
            .orElseThrow(() -> AcademicLedgerErrors.badRequest(
                "Academic ledger upload referenced by GPA summary was not found."));
        AcademicRecordSourceResponse source =
            new AcademicRecordSourceResponse(upload.getId(), upload.getCommittedAt());
        return new GpaSummaryResponse(
            summary.getStudentId(),
            "AVAILABLE",
            summary.getComputerScienceGpa(),
            summary.getTotalCredits(),
            summary.getCalculatedAt(),
            source);
    }

    private UUID currentStudentId() {
        CurrentActor actor = currentActorProvider.currentActor().orElseThrow(AcademicLedgerErrors::unauthorized);
        if (!actor.hasRole(RoleName.STUDENT)) {
            throw AcademicLedgerErrors.forbidden();
        }
        return studentRepository.findByUserAccountId(actor.userId())
            .orElseThrow(AcademicLedgerErrors::forbidden)
            .getId();
    }

    private int validatePage(Integer page) {
        int value = page == null ? 0 : page;
        if (value < 0) throw AcademicLedgerErrors.badRequest("page must be greater than or equal to 0.");
        return value;
    }

    private int validateSize(Integer size) {
        int value = size == null ? 20 : size;
        if (value < 1 || value > 100) throw AcademicLedgerErrors.badRequest("size must be between 1 and 100.");
        return value;
    }

    private String validateSearch(String search) {
        if (search == null) return null;
        String value = search.trim();
        if (value.isEmpty() || value.length() > 120) {
            throw AcademicLedgerErrors.badRequest("search must contain between 1 and 120 characters.");
        }
        return value.toLowerCase(java.util.Locale.ROOT);
    }
}
