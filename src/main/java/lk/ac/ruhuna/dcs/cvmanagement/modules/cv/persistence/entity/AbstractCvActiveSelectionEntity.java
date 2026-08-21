package lk.ac.ruhuna.dcs.cvmanagement.modules.cv.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.MappedSuperclass;
import java.util.UUID;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@MappedSuperclass
@Getter
@Setter
@NoArgsConstructor
public abstract class AbstractCvActiveSelectionEntity {
    @EmbeddedId
    private CvActiveSelectionId id;

    @Column(name = "student_id", nullable = false, updatable = false)
    private UUID studentId;

    protected AbstractCvActiveSelectionEntity(UUID cvId, UUID studentId, UUID sourceRecordId) {
        this.id = new CvActiveSelectionId(cvId, sourceRecordId);
        this.studentId = studentId;
    }
}
