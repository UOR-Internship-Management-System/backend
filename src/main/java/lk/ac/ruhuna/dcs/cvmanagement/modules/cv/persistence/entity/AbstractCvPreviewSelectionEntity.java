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
public abstract class AbstractCvPreviewSelectionEntity {
    @EmbeddedId
    private CvPreviewSelectionId id;

    @Column(name = "student_id", nullable = false, updatable = false)
    private UUID studentId;

    protected AbstractCvPreviewSelectionEntity(UUID previewId, UUID studentId, UUID sourceRecordId) {
        this.id = new CvPreviewSelectionId(previewId, sourceRecordId);
        this.studentId = studentId;
    }
}
