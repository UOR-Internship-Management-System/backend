package lk.ac.ruhuna.dcs.cvmanagement.modules.academics.api.dto.response;

import java.time.OffsetDateTime;
import java.util.UUID;
import lk.ac.ruhuna.dcs.cvmanagement.modules.academics.domain.AcademicLedgerUploadStatus;
import lk.ac.ruhuna.dcs.cvmanagement.modules.academics.domain.AcademicLedgerValidationStatus;

public record AcademicLedgerUploadDetailResponse(
        UUID uploadId,
        String originalFilename,
        String contentType,
        long fileSizeBytes,
        AcademicLedgerUploadStatus uploadStatus,
        AcademicLedgerValidationStatus validationStatus,
        int totalRows,
        int validRows,
        int invalidRows,
        OffsetDateTime uploadedAt,
        OffsetDateTime committedAt,
        String failureSummary,
        String statusMessage,
        Integer nextPollAfterSeconds) {
}
