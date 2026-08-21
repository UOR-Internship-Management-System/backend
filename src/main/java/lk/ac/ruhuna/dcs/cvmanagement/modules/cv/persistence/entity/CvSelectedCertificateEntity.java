package lk.ac.ruhuna.dcs.cvmanagement.modules.cv.persistence.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.util.UUID;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "cv_selected_certificates")
@NoArgsConstructor
public class CvSelectedCertificateEntity extends AbstractCvActiveSelectionEntity {
    public CvSelectedCertificateEntity(UUID cvId, UUID studentId, UUID sourceRecordId) {
        super(cvId, studentId, sourceRecordId);
    }
}
