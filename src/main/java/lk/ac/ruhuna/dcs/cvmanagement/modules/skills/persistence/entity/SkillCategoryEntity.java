package lk.ac.ruhuna.dcs.cvmanagement.modules.skills.persistence.entity;

import jakarta.persistence.*;
import java.util.UUID;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "skill_categories")
@Getter
@Setter
@NoArgsConstructor
public class SkillCategoryEntity {

    @Id
    private UUID id;

    @Column(name = "core_cluster_id", nullable = false)
    private UUID coreClusterId;

    @Column(name = "category_name", nullable = false)
    private String categoryName;

    @Column(name = "description")
    private String description;

    @Column(name = "display_order")
    private Integer displayOrder;
}
