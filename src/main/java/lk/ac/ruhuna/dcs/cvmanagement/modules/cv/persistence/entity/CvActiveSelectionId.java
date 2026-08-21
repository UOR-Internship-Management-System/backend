package lk.ac.ruhuna.dcs.cvmanagement.modules.cv.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Embeddable
@Getter
@Setter
@NoArgsConstructor
public class CvActiveSelectionId implements Serializable {
    @Column(name = "cv_id", nullable = false)
    private UUID cvId;

    @Column(name = "source_record_id", nullable = false)
    private UUID sourceRecordId;

    public CvActiveSelectionId(UUID cvId, UUID sourceRecordId) {
        this.cvId = cvId;
        this.sourceRecordId = sourceRecordId;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) return true;
        if (!(other instanceof CvActiveSelectionId that)) return false;
        return Objects.equals(cvId, that.cvId) && Objects.equals(sourceRecordId, that.sourceRecordId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(cvId, sourceRecordId);
    }
}
