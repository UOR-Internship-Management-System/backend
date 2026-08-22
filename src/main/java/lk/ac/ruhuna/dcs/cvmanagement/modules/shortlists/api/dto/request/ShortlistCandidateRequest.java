package lk.ac.ruhuna.dcs.cvmanagement.modules.shortlists.api.dto.request;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;
import java.util.UUID;

/** Bounded batch of manually selected Students. */
public record ShortlistCandidateRequest(
        @NotEmpty @Size(max = 100) List<@NotNull UUID> studentIds,
        @Size(max = 1000) String note) {

    public ShortlistCandidateRequest {
        studentIds = studentIds == null ? null : List.copyOf(studentIds);
    }
}
