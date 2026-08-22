package lk.ac.ruhuna.dcs.cvmanagement.modules.exports.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;
import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

/** Missing-CV snapshot retained with a bulk export job. */
@Entity
@Table(name = "export_missing_cv_students")
@IdClass(ExportFileEntity.Id.class)
public class ExportFileEntity {
    @jakarta.persistence.Id @Column(name = "export_job_id") private UUID exportJobId;
    @jakarta.persistence.Id @Column(name = "student_id") private UUID studentId;
    @Column(name = "index_number", nullable = false, length = 30) private String indexNumber;
    @Column(name = "full_name", nullable = false, length = 150) private String fullName;
    public ExportFileEntity() {}
    public UUID getExportJobId() { return exportJobId; } public void setExportJobId(UUID v) { exportJobId = v; }
    public UUID getStudentId() { return studentId; } public void setStudentId(UUID v) { studentId = v; }
    public String getIndexNumber() { return indexNumber; } public void setIndexNumber(String v) { indexNumber = v; }
    public String getFullName() { return fullName; } public void setFullName(String v) { fullName = v; }
    public static final class Id implements Serializable {
        private UUID exportJobId; private UUID studentId;
        public Id() {}
        @Override public boolean equals(Object other) { return other instanceof Id id && Objects.equals(exportJobId, id.exportJobId) && Objects.equals(studentId, id.studentId); }
        @Override public int hashCode() { return Objects.hash(exportJobId, studentId); }
    }
}
