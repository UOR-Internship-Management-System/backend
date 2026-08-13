package lk.ac.ruhuna.dcs.cvmanagement.modules.academics.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Derived Computer Science GPA read model. It is never independently user-editable. */
@Entity
@Table(name = "student_academic_summary", schema = "academic")
@Getter
@Setter
@NoArgsConstructor
public class StudentAcademicSummaryEntity {

    @Id
    @Column(name = "student_id", nullable = false, updatable = false)
    private UUID studentId;

    @Column(name = "computer_science_gpa", nullable = false, precision = 3, scale = 2)
    private BigDecimal computerScienceGpa;

    @Column(name = "total_credits", nullable = false, precision = 5, scale = 1)
    private BigDecimal totalCredits;

    @Column(name = "calculated_at", nullable = false)
    private OffsetDateTime calculatedAt;

    @Column(name = "source_upload_id", nullable = false)
    private UUID sourceUploadId;
}
