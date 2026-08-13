package lk.ac.ruhuna.dcs.cvmanagement.modules.academics.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.util.UUID;
import lk.ac.ruhuna.dcs.cvmanagement.modules.academics.domain.AcademicCourseType;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Canonical, curriculum-versioned BCS Computer Science subject definition. */
@Entity
@Table(name = "subject", schema = "academic")
@Getter
@Setter
@NoArgsConstructor
public class SubjectEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "subject_id", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "catalog_version", nullable = false, length = 50, updatable = false)
    private String catalogVersion;

    @Column(name = "cohort_start_year", nullable = false, updatable = false)
    private short cohortStartYear;

    @Column(name = "cohort_end_year", updatable = false)
    private Short cohortEndYear;

    @Column(name = "course_code", nullable = false, length = 30, updatable = false)
    private String courseCode;

    @Column(name = "course_title", nullable = false, length = 250)
    private String courseTitle;

    @Column(name = "credits", nullable = false, precision = 4, scale = 1)
    private BigDecimal credits;

    @Column(name = "academic_level", nullable = false)
    private short academicLevel;

    @Column(name = "semester", nullable = false, length = 80)
    private String semester;

    @Enumerated(EnumType.STRING)
    @Column(name = "course_type", nullable = false, length = 20)
    private AcademicCourseType courseType;

    @Column(name = "is_active", nullable = false)
    private boolean active = true;
}
