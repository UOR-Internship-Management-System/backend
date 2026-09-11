package lk.ac.ruhuna.dcs.cvmanagement.modules.studentprofile.persistence.entity;

import jakarta.persistence.*;

import java.time.OffsetDateTime;
import java.util.UUID;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "student_contact_links")
@Getter
@Setter
@NoArgsConstructor
public class ContactLinkEntity {

    @Id
    private UUID id;

    @Column(name = "student_id", nullable = false)
    private UUID studentId;

    @Column(name = "label", nullable = false)
    private String label;

    @Column(name = "url", nullable = false)
    private String url;

    @Column(name = "display_order")
    private Integer displayOrder;

    @Column(name = "cv_include")
    private boolean cvInclude;

    @Column(name = "created_at", updatable = false)
    private OffsetDateTime createdAt;

    @Column(name = "updated_at")
    private OffsetDateTime updatedAt;

    @Version
    @Column(name = "version", nullable = false)
    private Long version;
}
