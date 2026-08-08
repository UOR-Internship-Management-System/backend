package lk.ac.ruhuna.dcs.cvmanagement.modules.skills.persistence.entity;

import jakarta.persistence.*;
import java.time.OffsetDateTime;
import java.util.UUID;
import lk.ac.ruhuna.dcs.cvmanagement.modules.skills.domain.CompetencyLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "student_declared_skills")
@Getter
@Setter
@NoArgsConstructor
public class DeclaredSkillEntity {

    @Id
    private UUID id;

    @Column(name = "student_id", nullable = false)
    private UUID studentId;

    @Column(name = "skill_id", nullable = false)
    private UUID skillId;

    @Enumerated(EnumType.STRING)
    @Column(name = "competency_level", nullable = false)
    private CompetencyLevel competencyLevel;

    @Version
    @Column(name = "version", nullable = false)
    private Long version;

    @Column(name = "created_at", updatable = false)
    private OffsetDateTime createdAt;

    @Column(name = "updated_at")
    private OffsetDateTime updatedAt;
}
