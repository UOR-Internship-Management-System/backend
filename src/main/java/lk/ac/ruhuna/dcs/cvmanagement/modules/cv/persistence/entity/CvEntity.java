package lk.ac.ruhuna.dcs.cvmanagement.modules.cv.persistence.entity;

import jakarta.persistence.*;
import java.time.OffsetDateTime;
import java.util.UUID;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

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

    @Column(name = "created_at", updatable = false)
    private OffsetDateTime createdAt;

    @Column(name = "generated_at")
    private OffsetDateTime generatedAt;

    @Column(name = "saved_at")
    private OffsetDateTime savedAt;

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
    private long pdfFileSizeBytes;
}
