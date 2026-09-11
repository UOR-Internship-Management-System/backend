package lk.ac.ruhuna.dcs.cvmanagement.modules.academics.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.OffsetDateTime;
import java.util.UUID;
import lk.ac.ruhuna.dcs.cvmanagement.modules.academics.domain.AcademicLedgerUploadStatus;
import lk.ac.ruhuna.dcs.cvmanagement.modules.academics.domain.AcademicLedgerValidationStatus;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.type.SqlTypes;

/**
 * Durable Academic Ledger batch record.
 *
 * <p>The row is the source of truth for asynchronous processing and commit lifecycle state.
 */
@Entity
@Table(name = "academic_ledger_upload", schema = "academic")
@Getter
@Setter
@NoArgsConstructor
public class AcademicLedgerUploadEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "academic_ledger_upload_id", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "uploaded_by_account_id", nullable = false, updatable = false)
    private UUID uploadedByAccountId;

    @Column(name = "source_file_asset_id", nullable = false, updatable = false)
    private UUID sourceFileAssetId;

    @Column(name = "file_name", nullable = false, length = 255, updatable = false)
    private String fileName;

    @Column(name = "file_hash", nullable = false, length = 64, updatable = false, columnDefinition = "char(64)")
    @JdbcTypeCode(SqlTypes.CHAR)
    private String fileHash;

    @Enumerated(EnumType.STRING)
    @Column(name = "upload_status", nullable = false, length = 30)
    private AcademicLedgerUploadStatus uploadStatus = AcademicLedgerUploadStatus.RECEIVED;

    @Enumerated(EnumType.STRING)
    @Column(name = "validation_status", nullable = false, length = 30)
    private AcademicLedgerValidationStatus validationStatus = AcademicLedgerValidationStatus.NOT_STARTED;

    @Column(name = "total_rows", nullable = false)
    private int totalRows;

    @Column(name = "valid_rows", nullable = false)
    private int validRows;

    @Column(name = "invalid_rows", nullable = false)
    private int invalidRows;

    @Column(name = "failure_summary", length = 500)
    private String failureSummary;

    @Column(name = "processing_started_at")
    private OffsetDateTime processingStartedAt;

    @Column(name = "validation_completed_at")
    private OffsetDateTime validationCompletedAt;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    @Column(name = "committed_at")
    private OffsetDateTime committedAt;
}
