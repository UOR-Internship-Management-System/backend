package lk.ac.ruhuna.dcs.cvmanagement.modules.internships.persistence.projection;

import java.time.OffsetDateTime;
import java.util.UUID;

/** Module-local immutable snapshot of Company metadata needed by Internship responses. */
public record CompanySnapshotProjection(
        UUID companyId,
        String name,
        String websiteUrl,
        String contactPerson,
        String contactEmail,
        String contactPhone,
        String notes,
        long version,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt) {
}
