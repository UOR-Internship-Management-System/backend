package lk.ac.ruhuna.dcs.cvmanagement.modules.studentprofile.persistence.entity;

import jakarta.persistence.*;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "student_awards")
@Getter
@Setter
@NoArgsConstructor
public class AwardEntity {

    @Id
    private UUID id;

    @Column(name = "student_id", nullable = false)
    private UUID studentId;

    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "issuer", nullable = false)
    private String issuer;

    @Column(name = "award_date", nullable = false)
    private LocalDate awardDate;

    @Column(name = "description")
    private String description;

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
