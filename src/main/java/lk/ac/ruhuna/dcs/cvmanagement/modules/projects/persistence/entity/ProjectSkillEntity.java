package lk.ac.ruhuna.dcs.cvmanagement.modules.projects.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "student_project_skills")
@Getter
@Setter
@NoArgsConstructor
public class ProjectSkillEntity {

    @EmbeddedId
    private ProjectSkillId id;

    public ProjectSkillEntity(UUID projectId, UUID skillId) {
        this.id = new ProjectSkillId(projectId, skillId);
    }

    @Embeddable
    @Getter
    @Setter
    @NoArgsConstructor
    public static class ProjectSkillId implements Serializable {

        @Column(name = "project_id")
        private UUID projectId;

        @Column(name = "skill_id")
        private UUID skillId;

        public ProjectSkillId(UUID projectId, UUID skillId) {
            this.projectId = projectId;
            this.skillId = skillId;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof ProjectSkillId that)) return false;
            return Objects.equals(projectId, that.projectId) && Objects.equals(skillId, that.skillId);
        }

        @Override
        public int hashCode() {
            return Objects.hash(projectId, skillId);
        }
    }
}
