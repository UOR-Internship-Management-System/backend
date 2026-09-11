package lk.ac.ruhuna.dcs.cvmanagement.modules.academics.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Immutable official grade history produced only by a successful ledger commit. */
@Entity
@Table(name = "official_student_grade", schema = "academic")
@Getter
@Setter
@NoArgsConstructor
public class OfficialStudentGradeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "official_student_grade_id", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "student_id", nullable = false, updatable = false)
    private UUID studentId;

    @Column(name = "subject_id", nullable = false, updatable = false)
    private UUID subjectId;

    @Column(name = "academic_ledger_upload_id", nullable = false, updatable = false)
    private UUID academicLedgerUploadId;

    @Column(name = "semester", nullable = false, length = 80, updatable = false)
    private String semester;

    @Column(name = "academic_year", nullable = false, length = 9, updatable = false)
    private String academicYear;

    @Column(name = "attempt_number", nullable = false, updatable = false)
    private short attemptNumber;

    @Column(name = "credits", nullable = false, precision = 4, scale = 1, updatable = false)
    private BigDecimal credits;

    @Column(name = "grade_point", nullable = false, precision = 3, scale = 2, updatable = false)
    private BigDecimal gradePoint;

    @Column(name = "letter_grade", nullable = false, length = 5, updatable = false)
    private String letterGrade;

    @Column(name = "result_status", nullable = false, length = 30, updatable = false)
    private String resultStatus;

    @Column(name = "committed_at", nullable = false, updatable = false)
    private OffsetDateTime committedAt;
}
