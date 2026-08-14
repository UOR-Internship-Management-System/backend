package lk.ac.ruhuna.dcs.cvmanagement.modules.adminstudents.persistence.projection;

import java.time.OffsetDateTime;
import java.util.UUID;

/** Immutable database projection for the read-only Admin Student profile summary. */
public record AdminStudentProfileRow(
        UUID studentId,
        String fullName,
        String indexNumber,
        String universityEmail,
        int studentLevel,
        Integer cohortYear,
        String personalEmail,
        String headline,
        String summary,
        String phone,
        String location,
        long version,
        OffsetDateTime updatedAt,
        OffsetDateTime cvSourceUpdatedAt) {
}
