package lk.ac.ruhuna.dcs.cvmanagement.modules.exports.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import java.time.OffsetDateTime;
import java.util.UUID;
import lk.ac.ruhuna.dcs.cvmanagement.modules.exports.domain.policy.ExportFormat;
import lk.ac.ruhuna.dcs.cvmanagement.modules.exports.domain.policy.ExportStatus;
import lk.ac.ruhuna.dcs.cvmanagement.modules.exports.domain.policy.ExportType;

@Entity
@Table(name = "export_jobs")
public class ExportJobEntity {
    @Id private UUID id;
    @Column(name = "shortlist_id", nullable = false) private UUID shortlistId;
    @Enumerated(EnumType.STRING) @Column(name = "export_type", nullable = false, length = 40) private ExportType exportType;
    @Enumerated(EnumType.STRING) @Column(name = "format", nullable = false, length = 10) private ExportFormat format;
    @Enumerated(EnumType.STRING) @Column(name = "status", nullable = false, length = 20) private ExportStatus status;
    @Column(name = "requested_by_account_id", nullable = false) private UUID requestedByAccountId;
    @Column(name = "file_asset_id") private UUID fileAssetId;
    @Column(name = "total_candidate_count", nullable = false) private int totalCandidateCount;
    @Column(name = "included_file_count", nullable = false) private int includedFileCount;
    @Column(name = "missing_cv_count", nullable = false) private int missingCvCount;
    @Column(name = "failure_code", length = 100) private String failureCode;
    @Column(name = "failure_message", length = 500) private String failureMessage;
    @Version @Column(name = "version", nullable = false) private Long version;
    @Column(name = "created_at", nullable = false, updatable = false) private OffsetDateTime createdAt;
    @Column(name = "started_at") private OffsetDateTime startedAt;
    @Column(name = "completed_at") private OffsetDateTime completedAt;
    @Column(name = "expires_at") private OffsetDateTime expiresAt;
    public ExportJobEntity() {}
    public UUID getId() { return id; } public void setId(UUID v) { id = v; }
    public UUID getShortlistId() { return shortlistId; } public void setShortlistId(UUID v) { shortlistId = v; }
    public ExportType getExportType() { return exportType; } public void setExportType(ExportType v) { exportType = v; }
    public ExportFormat getFormat() { return format; } public void setFormat(ExportFormat v) { format = v; }
    public ExportStatus getStatus() { return status; } public void setStatus(ExportStatus v) { status = v; }
    public UUID getRequestedByAccountId() { return requestedByAccountId; } public void setRequestedByAccountId(UUID v) { requestedByAccountId = v; }
    public UUID getFileAssetId() { return fileAssetId; } public void setFileAssetId(UUID v) { fileAssetId = v; }
    public int getTotalCandidateCount() { return totalCandidateCount; } public void setTotalCandidateCount(int v) { totalCandidateCount = v; }
    public int getIncludedFileCount() { return includedFileCount; } public void setIncludedFileCount(int v) { includedFileCount = v; }
    public int getMissingCvCount() { return missingCvCount; } public void setMissingCvCount(int v) { missingCvCount = v; }
    public String getFailureCode() { return failureCode; } public void setFailureCode(String v) { failureCode = v; }
    public String getFailureMessage() { return failureMessage; } public void setFailureMessage(String v) { failureMessage = v; }
    public Long getVersion() { return version; } public void setVersion(Long v) { version = v; }
    public OffsetDateTime getCreatedAt() { return createdAt; } public void setCreatedAt(OffsetDateTime v) { createdAt = v; }
    public OffsetDateTime getStartedAt() { return startedAt; } public void setStartedAt(OffsetDateTime v) { startedAt = v; }
    public OffsetDateTime getCompletedAt() { return completedAt; } public void setCompletedAt(OffsetDateTime v) { completedAt = v; }
    public OffsetDateTime getExpiresAt() { return expiresAt; } public void setExpiresAt(OffsetDateTime v) { expiresAt = v; }
}
