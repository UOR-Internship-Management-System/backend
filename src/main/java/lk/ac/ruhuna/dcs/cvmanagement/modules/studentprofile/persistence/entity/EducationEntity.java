package lk.ac.ruhuna.dcs.cvmanagement.modules.studentprofile.persistence.entity;

import jakarta.persistence.*;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "student_education")
@Getter
@Setter
@NoArgsConstructor
public class EducationEntity {

    @Id
    private UUID id;

    @Column(name = "student_id", nullable = false)
    private UUID studentId;

    @Column(name = "degree", nullable = false)
    private String degree;

    @Column(name = "institution", nullable = false)
    private String institution;

    @Column(name = "institution_url")
    private String institutionUrl;

    @Column(name = "location")
    private String location;

    @Column(name = "start_date")
    private LocalDate startDate;

    @Column(name = "end_date")
    private LocalDate endDate;

    @Column(name = "is_current")
    private boolean current;

    @Column(name = "result_note")
    private String resultNote;

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
