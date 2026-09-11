package lk.ac.ruhuna.dcs.cvmanagement.modules.shortlists.api.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/** Finalization acknowledgement and optional audit note. */
public record ShortlistFinalizeRequest(
        @NotNull Boolean acknowledgeGuidanceWarning,
        @Size(max = 1000) String finalizationNote) {
}
