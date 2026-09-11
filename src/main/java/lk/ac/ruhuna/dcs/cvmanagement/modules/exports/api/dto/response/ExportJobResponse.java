package lk.ac.ruhuna.dcs.cvmanagement.modules.exports.api.dto.response;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
import lk.ac.ruhuna.dcs.cvmanagement.modules.exports.domain.policy.ExportFormat;
import lk.ac.ruhuna.dcs.cvmanagement.modules.exports.domain.policy.ExportStatus;
import lk.ac.ruhuna.dcs.cvmanagement.modules.exports.domain.policy.ExportType;

public record ExportJobResponse(
        UUID exportJobId,
        UUID shortlistId,
        ExportType exportType,
        ExportFormat format,
        ExportStatus status,
        int totalCandidateCount,
        int includedFileCount,
        int missingCvCount,
        List<MissingCvStudentResponse> missingCvStudents,
        List<ExportWarningResponse> warnings,
        boolean downloadReady,
        String downloadUrl,
        OffsetDateTime createdAt,
        OffsetDateTime startedAt,
        OffsetDateTime completedAt,
        OffsetDateTime expiresAt,
        String failureCode,
        String failureMessage) {
    public ExportJobResponse {
        missingCvStudents = List.copyOf(missingCvStudents);
        warnings = List.copyOf(warnings);
    }
}
