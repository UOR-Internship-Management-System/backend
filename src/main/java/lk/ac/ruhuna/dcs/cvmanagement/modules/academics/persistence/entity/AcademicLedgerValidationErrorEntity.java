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
import lk.ac.ruhuna.dcs.cvmanagement.modules.academics.domain.AcademicLedgerValidationSeverity;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

/** Persisted, structured validation diagnostic for one staging row. */
@Entity
@Table(name = "academic_ledger_validation_error", schema = "academic")
@Getter
@Setter
@NoArgsConstructor
public class AcademicLedgerValidationErrorEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "validation_error_id", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "staging_row_id", nullable = false, updatable = false)
    private UUID stagingRowId;

    @Column(name = "field_name", length = 80)
    private String fieldName;

    @Column(name = "error_code", nullable = false, length = 64, updatable = false)
    private String errorCode;

    @Column(name = "error_message", nullable = false, length = 300, updatable = false)
    private String errorMessage;

    @Enumerated(EnumType.STRING)
    @Column(name = "severity", nullable = false, length = 20, updatable = false)
    private AcademicLedgerValidationSeverity severity;

    @Column(name = "rejected_value", length = 120, updatable = false)
    private String rejectedValue;

    @Column(name = "related_row_number", updatable = false)
    private Integer relatedRowNumber;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;
}
