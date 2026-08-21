package lk.ac.ruhuna.dcs.cvmanagement.modules.cv.persistence.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.util.UUID;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "cv_preview_certificates")
@NoArgsConstructor
public class CvPreviewCertificateEntity extends AbstractCvPreviewSelectionEntity {
    public CvPreviewCertificateEntity(UUID previewId, UUID studentId, UUID sourceRecordId) {
        super(previewId, studentId, sourceRecordId);
    }
}
