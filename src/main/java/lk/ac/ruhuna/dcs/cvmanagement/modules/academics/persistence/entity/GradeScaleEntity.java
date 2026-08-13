package lk.ac.ruhuna.dcs.cvmanagement.modules.academics.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Developer-managed letter-grade to grade-point reference used by ledger validation. */
@Entity
@Table(name = "grade_scale", schema = "ref")
@Getter
@Setter
@NoArgsConstructor
public class GradeScaleEntity {

    @Id
    @Column(name = "grade_code", nullable = false, length = 5, updatable = false)
    private String gradeCode;

    @Column(name = "grade_point", nullable = false, precision = 3, scale = 2)
    private BigDecimal gradePoint;

    @Column(name = "is_passing", nullable = false)
    private boolean passing;

    @Column(name = "is_active", nullable = false)
    private boolean active;

    @Column(name = "minimum_mark")
    private Short minimumMark;

    @Column(name = "maximum_mark")
    private Short maximumMark;
}
