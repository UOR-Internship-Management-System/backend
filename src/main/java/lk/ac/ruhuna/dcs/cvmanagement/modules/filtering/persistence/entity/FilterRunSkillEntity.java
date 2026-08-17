package lk.ac.ruhuna.dcs.cvmanagement.modules.filtering.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

/**
 * Normalized skill criterion attached to a filtering run.
 *
 * <p>{@code criteriaSource} preserves whether a skill was submitted through requestSkillIds or
 * additionalSkillIds so the run contract can be reconstructed exactly.
 */
@Entity
@Table(name = "candidate_filter_run_skills")
public class FilterRunSkillEntity {

    @EmbeddedId
    private FilterRunSkillId id;

    @Column(name = "criteria_source", nullable = false, length = 10)
    private String criteriaSource;

    public FilterRunSkillEntity() {
        // Required by JPA.
    }

    public FilterRunSkillEntity(UUID filterRunId, UUID skillId, String criteriaSource) {
        this.id = new FilterRunSkillId(filterRunId, skillId);
        this.criteriaSource = criteriaSource;
    }

    public FilterRunSkillId getId() {
        return id;
    }

    public void setId(FilterRunSkillId id) {
        this.id = id;
    }

    public String getCriteriaSource() {
        return criteriaSource;
    }

    public void setCriteriaSource(String criteriaSource) {
        this.criteriaSource = criteriaSource;
    }

    @Embeddable
    public static class FilterRunSkillId implements Serializable {

        @Column(name = "filter_run_id", nullable = false)
        private UUID filterRunId;

        @Column(name = "skill_id", nullable = false)
        private UUID skillId;

        public FilterRunSkillId() {
            // Required by JPA.
        }

        public FilterRunSkillId(UUID filterRunId, UUID skillId) {
            this.filterRunId = filterRunId;
            this.skillId = skillId;
        }

        public UUID getFilterRunId() {
            return filterRunId;
        }

        public void setFilterRunId(UUID filterRunId) {
            this.filterRunId = filterRunId;
        }

        public UUID getSkillId() {
            return skillId;
        }

        public void setSkillId(UUID skillId) {
            this.skillId = skillId;
        }

        @Override
        public boolean equals(Object other) {
            if (this == other) {
                return true;
            }
            if (!(other instanceof FilterRunSkillId that)) {
                return false;
            }
            return Objects.equals(filterRunId, that.filterRunId)
                    && Objects.equals(skillId, that.skillId);
        }

        @Override
        public int hashCode() {
            return Objects.hash(filterRunId, skillId);
        }
    }
}
