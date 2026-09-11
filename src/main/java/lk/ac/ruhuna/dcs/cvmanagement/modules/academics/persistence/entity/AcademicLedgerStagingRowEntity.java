package lk.ac.ruhuna.dcs.cvmanagement.modules.academics.persistence.entity;

import com.fasterxml.jackson.databind.JsonNode;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;
import lk.ac.ruhuna.dcs.cvmanagement.modules.academics.domain.AcademicLedgerRowValidationStatus;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

/**
 * Normalized row staged from an uploaded ledger.
 *
 * <p>Staging rows are deliberately isolated from official academic records and may be deleted with their upload batch.
 */
@Entity
@Table(name = "academic_ledger_staging_row", schema = "academic")
@Getter
@Setter
@NoArgsConstructor
public class AcademicLedgerStagingRowEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "staging_row_id", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "academic_ledger_upload_id", nullable = false, updatable = false)
    private UUID academicLedgerUploadId;

    @Column(name = "row_number", nullable = false, updatable = false)
    private int rowNumber;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "raw_payload", nullable = false, columnDefinition = "jsonb", updatable = false)
    private JsonNode rawPayload;

    @Column(name = "student_index_number", nullable = false, length = 40, updatable = false)
    private String studentIndexNumber;

    @Column(name = "student_id")
    private UUID studentId;

    @Column(name = "course_code", nullable = false, length = 30, updatable = false)
    private String courseCode;

    @Column(name = "course_title", length = 250)
    private String courseTitle;

    @Column(name = "credits", nullable = false, precision = 4, scale = 1, updatable = false)
    private BigDecimal credits;

    @Column(name = "letter_grade", nullable = false, length = 5, updatable = false)
    private String letterGrade;

    @Column(name = "grade_point", precision = 3, scale = 2)
    private BigDecimal gradePoint;

    @Column(name = "semester", nullable = false, length = 80, updatable = false)
    private String semester;

    @Column(name = "academic_year", nullable = false, length = 9, updatable = false)
    private String academicYear;

    @Column(name = "attempt_number", nullable = false, updatable = false)
    private short attemptNumber;

    @Column(name = "result_status", nullable = false, length = 30, updatable = false)
    private String resultStatus;

    @Enumerated(EnumType.STRING)
    @Column(name = "validation_status", length = 20)
    private AcademicLedgerRowValidationStatus validationStatus;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;
}
