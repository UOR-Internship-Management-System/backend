package lk.ac.ruhuna.dcs.cvmanagement.modules.cv.persistence.entity;

import jakarta.persistence.*;
import java.time.OffsetDateTime;
import java.util.UUID;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "cv_source_freshness")
@Getter
@Setter
@NoArgsConstructor
public class CvSourceFreshnessEntity {

    @Id
    @Column(name = "student_id")
    private UUID studentId;

    @Column(name = "profile_changed_at")
    private OffsetDateTime profileChangedAt;

    @Column(name = "declared_skills_changed_at")
    private OffsetDateTime declaredSkillsChangedAt;

    @Column(name = "projects_changed_at")
    private OffsetDateTime projectsChangedAt;

    @Column(name = "academic_records_changed_at")
    private OffsetDateTime academicRecordsChangedAt;
}
