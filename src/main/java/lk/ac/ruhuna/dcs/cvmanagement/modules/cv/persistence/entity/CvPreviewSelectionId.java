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
public class CvPreviewSelectionId implements Serializable {
    @Column(name = "preview_id", nullable = false)
    private UUID previewId;

    @Column(name = "source_record_id", nullable = false)
    private UUID sourceRecordId;

    public CvPreviewSelectionId(UUID previewId, UUID sourceRecordId) {
        this.previewId = previewId;
        this.sourceRecordId = sourceRecordId;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) return true;
        if (!(other instanceof CvPreviewSelectionId that)) return false;
        return Objects.equals(previewId, that.previewId) && Objects.equals(sourceRecordId, that.sourceRecordId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(previewId, sourceRecordId);
    }
}
