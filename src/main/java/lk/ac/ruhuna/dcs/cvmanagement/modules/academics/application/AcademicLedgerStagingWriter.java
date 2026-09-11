package lk.ac.ruhuna.dcs.cvmanagement.modules.academics.application;

import java.util.List;
import java.util.UUID;
import lk.ac.ruhuna.dcs.cvmanagement.modules.academics.persistence.entity.AcademicLedgerStagingRowEntity;
import lk.ac.ruhuna.dcs.cvmanagement.modules.academics.persistence.repository.AcademicLedgerStagingRowRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/** Writes each parser batch in its own transaction so the persistence context remains bounded. */
@Service
class AcademicLedgerStagingWriter {

    private final AcademicLedgerStagingRowRepository stagingRepository;

    AcademicLedgerStagingWriter(AcademicLedgerStagingRowRepository stagingRepository) {
        this.stagingRepository = stagingRepository;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    void write(UUID uploadId, List<AcademicLedgerParsedRow> rows) {
        List<AcademicLedgerStagingRowEntity> entities = rows.stream().map(row -> toEntity(uploadId, row)).toList();
        stagingRepository.saveAllAndFlush(entities);
    }

    private AcademicLedgerStagingRowEntity toEntity(UUID uploadId, AcademicLedgerParsedRow row) {
        AcademicLedgerStagingRowEntity entity = new AcademicLedgerStagingRowEntity();
        entity.setAcademicLedgerUploadId(uploadId);
        entity.setRowNumber(row.rowNumber());
        entity.setRawPayload(row.rawPayload());
        entity.setStudentIndexNumber(row.studentIndexNumber());
        entity.setCourseCode(row.courseCode());
        entity.setCredits(row.credits());
        entity.setLetterGrade(row.letterGrade());
        entity.setSemester(row.semester());
        entity.setAcademicYear(row.academicYear());
        entity.setAttemptNumber(row.attemptNumber());
        entity.setResultStatus(row.resultStatus());
        return entity;
    }
}
