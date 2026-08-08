package lk.ac.ruhuna.dcs.cvmanagement.modules.studentprofile.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
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
public class StudentEntity {

    @Id
    private UUID id;

    @Column(name = "index_number", nullable = false, updatable = false)
    private String indexNumber;

    @Column(name = "university_email", nullable = false, updatable = false)
    private String universityEmail;

    @Column(name = "full_name", nullable = false)
    private String fullName;

    @Column(name = "academic_level", nullable = false, updatable = false)
    private Short academicLevel;

    @Column(name = "is_active", nullable = false)
    private boolean active;

    @Column(name = "user_account_id")
    private UUID userAccountId;

    @Column(name = "created_at", updatable = false)
    private OffsetDateTime createdAt;

    @Column(name = "updated_at")
    private OffsetDateTime updatedAt;
}
