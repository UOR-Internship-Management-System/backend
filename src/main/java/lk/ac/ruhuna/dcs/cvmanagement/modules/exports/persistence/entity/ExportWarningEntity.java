package lk.ac.ruhuna.dcs.cvmanagement.modules.exports.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;
import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;
import lk.ac.ruhuna.dcs.cvmanagement.modules.exports.domain.policy.ExportWarningCode;

@Entity
@Table(name = "export_warnings")
@IdClass(ExportWarningEntity.Id.class)
public class ExportWarningEntity {
    @jakarta.persistence.Id @Column(name = "export_job_id") private UUID exportJobId;
    @jakarta.persistence.Id @Enumerated(EnumType.STRING) @Column(name = "warning_code", length = 40) private ExportWarningCode warningCode;
    @Column(name = "message", nullable = false, length = 500) private String message;
    public ExportWarningEntity() {}
    public UUID getExportJobId() { return exportJobId; } public void setExportJobId(UUID v) { exportJobId = v; }
    public ExportWarningCode getWarningCode() { return warningCode; } public void setWarningCode(ExportWarningCode v) { warningCode = v; }
    public String getMessage() { return message; } public void setMessage(String v) { message = v; }
    public static final class Id implements Serializable {
        private UUID exportJobId; private ExportWarningCode warningCode;
        public Id() {}
        @Override public boolean equals(Object other) { return other instanceof Id id && Objects.equals(exportJobId, id.exportJobId) && warningCode == id.warningCode; }
        @Override public int hashCode() { return Objects.hash(exportJobId, warningCode); }
    }
}
