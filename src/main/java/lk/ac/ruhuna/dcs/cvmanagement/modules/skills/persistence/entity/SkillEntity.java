package lk.ac.ruhuna.dcs.cvmanagement.modules.skills.persistence.entity;

import jakarta.persistence.*;
import java.util.UUID;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "skills")
@Getter
@Setter
@NoArgsConstructor
public class SkillEntity {

    @Id
    private UUID id;

    @Column(name = "skill_category_id", nullable = false)
    private UUID skillCategoryId;

    @Column(name = "skill_name", nullable = false, unique = true)
    private String skillName;

    @Column(name = "skill_description")
    private String skillDescription;

    @Column(name = "skill_status", nullable = false)
    private String skillStatus;

    @Column(name = "display_order")
    private Integer displayOrder;
}
