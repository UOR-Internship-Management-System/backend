package lk.ac.ruhuna.dcs.cvmanagement.modules.companies.api.dto.response;

import java.time.OffsetDateTime;
import java.util.UUID;

/** Public Company metadata response defined by OpenAPI v1.6.0. */
public record CompanyResponse(
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
