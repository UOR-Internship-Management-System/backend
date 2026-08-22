package lk.ac.ruhuna.dcs.cvmanagement.modules.exports.mapper;

import java.util.List;
import lk.ac.ruhuna.dcs.cvmanagement.modules.exports.api.dto.response.ExportJobResponse;
import lk.ac.ruhuna.dcs.cvmanagement.modules.exports.api.dto.response.ExportWarningResponse;
import lk.ac.ruhuna.dcs.cvmanagement.modules.exports.api.dto.response.MissingCvStudentResponse;
import lk.ac.ruhuna.dcs.cvmanagement.modules.exports.domain.policy.ExportStatus;
import lk.ac.ruhuna.dcs.cvmanagement.modules.exports.domain.policy.ExportType;
import lk.ac.ruhuna.dcs.cvmanagement.modules.exports.persistence.entity.ExportFileEntity;
import lk.ac.ruhuna.dcs.cvmanagement.modules.exports.persistence.entity.ExportJobEntity;
import lk.ac.ruhuna.dcs.cvmanagement.modules.exports.persistence.entity.ExportWarningEntity;
import lk.ac.ruhuna.dcs.cvmanagement.shared.api.ApiPaths;
import org.springframework.stereotype.Component;

@Component
public class ExportMapper {
    public ExportJobResponse toResponse(ExportJobEntity job, List<ExportFileEntity> missing, List<ExportWarningEntity> warnings) {
        boolean ready = job.getStatus() == ExportStatus.COMPLETED && job.getFileAssetId() != null;
        String suffix = job.getExportType() == ExportType.BULK_LATEST_CV_ZIP ? "/bulk-cvs/download" : "/download";
        return new ExportJobResponse(
                job.getId(), job.getShortlistId(), job.getExportType(), job.getFormat(), job.getStatus(),
                job.getTotalCandidateCount(), job.getIncludedFileCount(), job.getMissingCvCount(),
                missing.stream().map(value -> new MissingCvStudentResponse(value.getStudentId(), value.getIndexNumber(), value.getFullName())).toList(),
                warnings.stream().map(value -> new ExportWarningResponse(value.getWarningCode(), value.getMessage())).toList(),
                ready,
                ready ? ApiPaths.ADMIN_EXPORTS + "/" + job.getId() + suffix : null,
                job.getCreatedAt(), job.getStartedAt(), job.getCompletedAt(), job.getExpiresAt(),
                job.getFailureCode(), job.getFailureMessage());
    }
}
