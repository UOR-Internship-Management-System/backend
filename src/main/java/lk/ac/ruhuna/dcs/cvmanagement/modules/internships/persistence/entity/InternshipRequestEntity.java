package lk.ac.ruhuna.dcs.cvmanagement.modules.internships.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import java.time.OffsetDateTime;
import java.util.UUID;

/** JPA persistence model for an Admin-managed Internship Request. */
@Entity
@Table(name = "internship_requests")
public class InternshipRequestEntity {

    @Id
    private UUID id;

    @Column(name = "company_id", nullable = false)
    private UUID companyId;

    @Column(name = "title", nullable = false, length = 200)
    private String title;

    @Column(name = "description", length = 10000)
    private String description;

    @Column(name = "shortlist_guidance_value")
    private Integer shortlistGuidanceValue;

    @Column(name = "created_by_account_id")
    private UUID createdByAccountId;

    @Version
    @Column(name = "version", nullable = false)
    private Long version;

    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    public InternshipRequestEntity() {
        // Required by JPA.
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public UUID getCompanyId() { return companyId; }
    public void setCompanyId(UUID companyId) { this.companyId = companyId; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public Integer getShortlistGuidanceValue() { return shortlistGuidanceValue; }
    public void setShortlistGuidanceValue(Integer shortlistGuidanceValue) { this.shortlistGuidanceValue = shortlistGuidanceValue; }
    public UUID getCreatedByAccountId() { return createdByAccountId; }
    public void setCreatedByAccountId(UUID createdByAccountId) { this.createdByAccountId = createdByAccountId; }
    public Long getVersion() { return version; }
    public void setVersion(Long version) { this.version = version; }
    public OffsetDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(OffsetDateTime createdAt) { this.createdAt = createdAt; }
    public OffsetDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(OffsetDateTime updatedAt) { this.updatedAt = updatedAt; }
}
