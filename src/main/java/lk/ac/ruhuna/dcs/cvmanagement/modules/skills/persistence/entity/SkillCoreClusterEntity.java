package lk.ac.ruhuna.dcs.cvmanagement.modules.skills.persistence.entity;

import jakarta.persistence.*;
import java.util.UUID;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "skill_core_clusters")
@Getter
@Setter
@NoArgsConstructor
public class SkillCoreClusterEntity {

    @Id
    private UUID id;

    @Column(name = "cluster_name", nullable = false, unique = true)
    private String clusterName;

    @Column(name = "description")
    private String description;

    @Column(name = "display_order")
    private Integer displayOrder;

    @Column(name = "is_active")
    private boolean active;
}
