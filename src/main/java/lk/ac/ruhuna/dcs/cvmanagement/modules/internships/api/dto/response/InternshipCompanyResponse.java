package lk.ac.ruhuna.dcs.cvmanagement.modules.internships.api.dto.response;

import java.time.OffsetDateTime;
import java.util.UUID;

/** Structural Company response embedded in an Internship Request without a cross-module Java dependency. */
public record InternshipCompanyResponse(
        UUID companyId, String name, String websiteUrl, String contactPerson, String contactEmail,
        String contactPhone, String notes, long version, OffsetDateTime createdAt, OffsetDateTime updatedAt) {
}
