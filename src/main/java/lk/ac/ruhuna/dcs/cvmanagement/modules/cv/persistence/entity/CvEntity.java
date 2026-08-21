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

@Entity
@Table(name = "cvs")
@Getter
@Setter
@NoArgsConstructor
public class CvEntity {

    @Id
    private UUID id;

    @Column(name = "student_id", nullable = false, unique = true)
    private UUID studentId;

    @Column(name = "revision", nullable = false)
    private int revision;

    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @Column(name = "generated_at", nullable = false)
    private OffsetDateTime generatedAt;

    @Column(name = "saved_at", nullable = false)
    private OffsetDateTime savedAt;

    /** Legacy V082 snapshot. New writes move to normalized selection tables in Patch 5. */
    @Column(name = "included_experience_ids")
    private String includedExperienceIds;

    @Column(name = "included_project_ids")
    private String includedProjectIds;

    @Column(name = "included_certificate_ids")
    private String includedCertificateIds;

    @Column(name = "included_award_ids")
    private String includedAwardIds;

    @Column(name = "included_activity_ids")
    private String includedActivityIds;

    @Column(name = "pdf_file_name")
    private String pdfFileName;

    @Column(name = "pdf_file_size_bytes")
    private Long pdfFileSizeBytes;

    @Column(name = "source_fingerprint", length = 64, columnDefinition = "char(64)")
    @JdbcTypeCode(SqlTypes.CHAR)
    private String sourceFingerprint;

    @Column(name = "pdf_file_asset_id")
    private UUID pdfFileAssetId;

    @Column(name = "last_saved_preview_id")
    private UUID lastSavedPreviewId;

    @Column(name = "updated_at")
    private OffsetDateTime updatedAt;
}
