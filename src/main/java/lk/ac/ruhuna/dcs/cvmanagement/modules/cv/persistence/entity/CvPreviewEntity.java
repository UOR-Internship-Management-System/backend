package lk.ac.ruhuna.dcs.cvmanagement.modules.cv.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.OffsetDateTime;
import java.util.UUID;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

/** Durable, owner-scoped preview lifecycle row. Generated document content itself is not stored in PostgreSQL. */
@Entity
@Table(name = "cv_previews")
@Getter
@Setter
@NoArgsConstructor
public class CvPreviewEntity {

    @Id
    @Column(name = "preview_id", nullable = false, updatable = false)
    private UUID previewId;

    @Column(name = "student_id", nullable = false, updatable = false)
    private UUID studentId;

    @Column(name = "source_fingerprint", nullable = false, length = 64, updatable = false, columnDefinition = "char(64)")
    @JdbcTypeCode(SqlTypes.CHAR)
    private String sourceFingerprint;

    @Column(name = "staged_storage_key", columnDefinition = "text")
    private String stagedStorageKey;

    @Column(name = "staged_file_name", length = 255)
    private String stagedFileName;

    @Column(name = "staged_file_size_bytes")
    private Long stagedFileSizeBytes;

    @Column(name = "staged_checksum_sha256", length = 64, columnDefinition = "char(64)")
    @JdbcTypeCode(SqlTypes.CHAR)
    private String stagedChecksumSha256;

    @Column(name = "generated_at", nullable = false, updatable = false)
    private OffsetDateTime generatedAt;

    @Column(name = "expires_at", nullable = false, updatable = false)
    private OffsetDateTime expiresAt;

    @Column(name = "consumed_at")
    private OffsetDateTime consumedAt;

    @Column(name = "result_cv_id")
    private UUID resultCvId;

    @Column(name = "result_revision")
    private Integer resultRevision;

    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;
}
