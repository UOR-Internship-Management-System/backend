package lk.ac.ruhuna.dcs.cvmanagement.modules.eligiblestudents.persistence.entity;

import jakarta.persistence.*;

import java.time.OffsetDateTime;
import java.util.UUID;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "eligible_students")
@Getter
@Setter
@NoArgsConstructor
public class EligibleStudentEntity {

    @Id
    private UUID id;

    @Column(name = "index_number", nullable = false, unique = true)
    private String indexNumber;

    @Column(name = "university_email", nullable = false, unique = true)
    private String universityEmail;

    @Column(name = "full_name", nullable = false)
    private String fullName;

    @Column(name = "academic_level", nullable = false)
    private short academicLevel;

    @Column(name = "is_active", nullable = false)
    private boolean active;

    @Column(name = "user_account_id")
    private UUID userAccountId;

    @Column(name = "created_at", updatable = false)
    private OffsetDateTime createdAt;

    @Column(name = "updated_at")
    private OffsetDateTime updatedAt;
}
