package lk.ac.ruhuna.dcs.cvmanagement.modules.internships.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.OffsetDateTime;
import java.util.UUID;

/** Persistence model for one normalized required-skill association. */
@Entity
@Table(name = "internship_request_skills")
public class InternshipRequestSkillEntity {

    @Id
    private UUID id;

    @Column(name = "internship_request_id", nullable = false)
    private UUID internshipRequestId;

    @Column(name = "skill_id", nullable = false)
    private UUID skillId;

    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    public InternshipRequestSkillEntity() {
        // Required by JPA.
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public UUID getInternshipRequestId() { return internshipRequestId; }
    public void setInternshipRequestId(UUID internshipRequestId) { this.internshipRequestId = internshipRequestId; }
    public UUID getSkillId() { return skillId; }
    public void setSkillId(UUID skillId) { this.skillId = skillId; }
    public OffsetDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(OffsetDateTime createdAt) { this.createdAt = createdAt; }
}
