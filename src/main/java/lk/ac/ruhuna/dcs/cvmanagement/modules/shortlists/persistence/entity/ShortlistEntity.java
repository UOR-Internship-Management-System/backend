package lk.ac.ruhuna.dcs.cvmanagement.modules.shortlists.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import java.time.OffsetDateTime;
import java.util.UUID;
import lk.ac.ruhuna.dcs.cvmanagement.modules.shortlists.domain.policy.ShortlistStatus;

/** JPA mutation model for a persisted shortlist. */
@Entity
@Table(name = "shortlists")
public class ShortlistEntity {

    @Id
    private UUID id;

    @Column(name = "internship_request_id", nullable = false, unique = true)
    private UUID internshipRequestId;

    @Column(name = "filter_run_id")
    private UUID filterRunId;

    @Column(name = "name", length = 200)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private ShortlistStatus status;

    @Column(name = "guidance_value_snapshot")
    private Integer guidanceValueSnapshot;

    @Column(name = "guidance_warning_acknowledged", nullable = false)
    private boolean guidanceWarningAcknowledged;

    @Column(name = "finalization_note", length = 1000)
    private String finalizationNote;

    @Column(name = "created_by_account_id", nullable = false)
    private UUID createdByAccountId;

    @Column(name = "finalized_by_account_id")
    private UUID finalizedByAccountId;

    @Version
    @Column(name = "version", nullable = false)
    private Long version;

    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    @Column(name = "finalized_at")
    private OffsetDateTime finalizedAt;

    public ShortlistEntity() {
        // Required by JPA.
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public UUID getInternshipRequestId() { return internshipRequestId; }
    public void setInternshipRequestId(UUID internshipRequestId) { this.internshipRequestId = internshipRequestId; }
    public UUID getFilterRunId() { return filterRunId; }
    public void setFilterRunId(UUID filterRunId) { this.filterRunId = filterRunId; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public ShortlistStatus getStatus() { return status; }
    public void setStatus(ShortlistStatus status) { this.status = status; }
    public Integer getGuidanceValueSnapshot() { return guidanceValueSnapshot; }
    public void setGuidanceValueSnapshot(Integer guidanceValueSnapshot) { this.guidanceValueSnapshot = guidanceValueSnapshot; }
    public boolean isGuidanceWarningAcknowledged() { return guidanceWarningAcknowledged; }
    public void setGuidanceWarningAcknowledged(boolean value) { this.guidanceWarningAcknowledged = value; }
    public String getFinalizationNote() { return finalizationNote; }
    public void setFinalizationNote(String finalizationNote) { this.finalizationNote = finalizationNote; }
    public UUID getCreatedByAccountId() { return createdByAccountId; }
    public void setCreatedByAccountId(UUID createdByAccountId) { this.createdByAccountId = createdByAccountId; }
    public UUID getFinalizedByAccountId() { return finalizedByAccountId; }
    public void setFinalizedByAccountId(UUID finalizedByAccountId) { this.finalizedByAccountId = finalizedByAccountId; }
    public Long getVersion() { return version; }
    public void setVersion(Long version) { this.version = version; }
    public OffsetDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(OffsetDateTime createdAt) { this.createdAt = createdAt; }
    public OffsetDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(OffsetDateTime updatedAt) { this.updatedAt = updatedAt; }
    public OffsetDateTime getFinalizedAt() { return finalizedAt; }
    public void setFinalizedAt(OffsetDateTime finalizedAt) { this.finalizedAt = finalizedAt; }
}
