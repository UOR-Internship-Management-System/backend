package lk.ac.ruhuna.dcs.cvmanagement.modules.internships.persistence.projection;

import java.time.OffsetDateTime;
import java.util.UUID;

/** Flattened bounded read-model row for one Internship Request plus its Company snapshot. */
public record InternshipRequestDetailProjection(
        UUID requestId,
        UUID companyId,
        String companyName,
        String companyWebsiteUrl,
        String companyContactPerson,
        String companyContactEmail,
        String companyContactPhone,
        String companyNotes,
        long companyVersion,
        OffsetDateTime companyCreatedAt,
        OffsetDateTime companyUpdatedAt,
        String title,
        String description,
        Integer shortlistGuidanceValue,
        long version,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt) {

    public CompanySnapshotProjection company() {
        return new CompanySnapshotProjection(
                companyId,
                companyName,
                companyWebsiteUrl,
                companyContactPerson,
                companyContactEmail,
                companyContactPhone,
                companyNotes,
                companyVersion,
                companyCreatedAt,
                companyUpdatedAt);
    }
}
