package lk.ac.ruhuna.dcs.cvmanagement.modules.shortlists.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.OffsetDateTime;
import java.util.UUID;

/** Persisted manual membership of one Student in one shortlist. */
@Entity
@Table(name = "shortlist_candidates")
public class ShortlistCandidateEntity {

    @Id
    private UUID id;

    @Column(name = "shortlist_id", nullable = false)
    private UUID shortlistId;

    @Column(name = "student_id", nullable = false)
    private UUID studentId;

    @Column(name = "selected_by_account_id", nullable = false)
    private UUID selectedByAccountId;

    @Column(name = "selected_at", nullable = false, updatable = false)
    private OffsetDateTime selectedAt;

    @Column(name = "selection_note", length = 1000)
    private String selectionNote;

    public ShortlistCandidateEntity() {
        // Required by JPA.
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public UUID getShortlistId() { return shortlistId; }
    public void setShortlistId(UUID shortlistId) { this.shortlistId = shortlistId; }
    public UUID getStudentId() { return studentId; }
    public void setStudentId(UUID studentId) { this.studentId = studentId; }
    public UUID getSelectedByAccountId() { return selectedByAccountId; }
    public void setSelectedByAccountId(UUID selectedByAccountId) { this.selectedByAccountId = selectedByAccountId; }
    public OffsetDateTime getSelectedAt() { return selectedAt; }
    public void setSelectedAt(OffsetDateTime selectedAt) { this.selectedAt = selectedAt; }
    public String getSelectionNote() { return selectionNote; }
    public void setSelectionNote(String selectionNote) { this.selectionNote = selectionNote; }
}
