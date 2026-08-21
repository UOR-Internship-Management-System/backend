package lk.ac.ruhuna.dcs.cvmanagement.modules.cv.persistence.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.util.UUID;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "cv_selected_awards")
@NoArgsConstructor
public class CvSelectedAwardEntity extends AbstractCvActiveSelectionEntity {
    public CvSelectedAwardEntity(UUID cvId, UUID studentId, UUID sourceRecordId) {
        super(cvId, studentId, sourceRecordId);
    }
}
