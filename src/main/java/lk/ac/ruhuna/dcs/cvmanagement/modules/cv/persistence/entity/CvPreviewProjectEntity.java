package lk.ac.ruhuna.dcs.cvmanagement.modules.cv.persistence.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.util.UUID;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "cv_preview_projects")
@NoArgsConstructor
public class CvPreviewProjectEntity extends AbstractCvPreviewSelectionEntity {
    public CvPreviewProjectEntity(UUID previewId, UUID studentId, UUID sourceRecordId) {
        super(previewId, studentId, sourceRecordId);
    }
}
