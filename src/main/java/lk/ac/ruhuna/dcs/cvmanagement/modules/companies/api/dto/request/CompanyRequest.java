package lk.ac.ruhuna.dcs.cvmanagement.modules.companies.api.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** Request body for creating Admin-managed Company metadata. */
public record CompanyRequest(
        @NotBlank(message = "Company name is required.")
        @Size(max = 200, message = "Company name must not exceed 200 characters.")
        String name,

        @Size(max = 500, message = "Website URL must not exceed 500 characters.")
        String websiteUrl,

        @Size(max = 150, message = "Contact person must not exceed 150 characters.")
        String contactPerson,

        @Email(message = "Contact email must be a valid email address.")
        @Size(max = 254, message = "Contact email must not exceed 254 characters.")
        String contactEmail,

        @Size(max = 30, message = "Contact phone must not exceed 30 characters.")
        String contactPhone,

        @Size(max = 4000, message = "Notes must not exceed 4000 characters.")
        String notes) {
}
