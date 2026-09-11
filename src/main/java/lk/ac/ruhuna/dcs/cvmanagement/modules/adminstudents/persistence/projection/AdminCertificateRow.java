package lk.ac.ruhuna.dcs.cvmanagement.modules.adminstudents.persistence.projection;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;

/** Immutable read projection for one Student certificate record. */
public record AdminCertificateRow(
        UUID id,
        String title,
        String issuer,
        LocalDate issueDate,
        String credentialUrl,
        boolean cvInclude,
        long version,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt) {
}
