package lk.ac.ruhuna.dcs.cvmanagement.modules.academics.api.dto.request;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotNull;

/** Explicit acknowledgement required before promoting staged academic data to official records. */
public record AcademicLedgerCommitRequest(
        @NotNull(message = "confirm is required.")
        @AssertTrue(message = "confirm must be true.")
        Boolean confirm) {
}
