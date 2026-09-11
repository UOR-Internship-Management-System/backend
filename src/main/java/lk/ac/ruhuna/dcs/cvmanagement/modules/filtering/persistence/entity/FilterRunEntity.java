package lk.ac.ruhuna.dcs.cvmanagement.modules.filtering.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;
import lk.ac.ruhuna.dcs.cvmanagement.modules.filtering.domain.policy.FilterSkillMatchMode;

/**
 * Immutable persistence model for one deterministic Candidate Filtering run.
 *
 * <p>The entity stores only sanitized criteria/history. Candidate result rows are deliberately not
 * persisted because the API recomputes them from the latest committed GPA and declared-skill data.
 */
@Entity
@Table(name = "candidate_filter_runs")
public class FilterRunEntity {

    @Id
    private UUID id;

    @Column(name = "internship_request_id", nullable = false)
    private UUID internshipRequestId;

    @Column(name = "run_by_account_id", nullable = false)
    private UUID runByAccountId;

    @Column(name = "runtime_gpa_lower_bound", precision = 3, scale = 2)
    private BigDecimal runtimeGpaLowerBound;

    @Column(name = "runtime_gpa_upper_bound", precision = 3, scale = 2)
    private BigDecimal runtimeGpaUpperBound;

    @Enumerated(EnumType.STRING)
    @Column(name = "skill_match_mode", nullable = false, length = 3)
    private FilterSkillMatchMode skillMatchMode;

    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    public FilterRunEntity() {
        // Required by JPA.
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getInternshipRequestId() {
        return internshipRequestId;
    }

    public void setInternshipRequestId(UUID internshipRequestId) {
        this.internshipRequestId = internshipRequestId;
    }

    public UUID getRunByAccountId() {
        return runByAccountId;
    }

    public void setRunByAccountId(UUID runByAccountId) {
        this.runByAccountId = runByAccountId;
    }

    public BigDecimal getRuntimeGpaLowerBound() {
        return runtimeGpaLowerBound;
    }

    public void setRuntimeGpaLowerBound(BigDecimal runtimeGpaLowerBound) {
        this.runtimeGpaLowerBound = runtimeGpaLowerBound;
    }

    public BigDecimal getRuntimeGpaUpperBound() {
        return runtimeGpaUpperBound;
    }

    public void setRuntimeGpaUpperBound(BigDecimal runtimeGpaUpperBound) {
        this.runtimeGpaUpperBound = runtimeGpaUpperBound;
    }

    public FilterSkillMatchMode getSkillMatchMode() {
        return skillMatchMode;
    }

    public void setSkillMatchMode(FilterSkillMatchMode skillMatchMode) {
        this.skillMatchMode = skillMatchMode;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(OffsetDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
