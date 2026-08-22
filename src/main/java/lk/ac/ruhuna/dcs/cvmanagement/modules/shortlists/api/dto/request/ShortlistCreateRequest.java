package lk.ac.ruhuna.dcs.cvmanagement.modules.shortlists.api.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.UUID;

/** Creates the single draft shortlist for an Internship Request. */
public record ShortlistCreateRequest(
        @NotNull UUID requestId,
        UUID filterRunId,
        @Size(max = 200) String name) {
}
