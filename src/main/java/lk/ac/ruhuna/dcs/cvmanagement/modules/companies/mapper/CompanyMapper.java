package lk.ac.ruhuna.dcs.cvmanagement.modules.companies.mapper;

import lk.ac.ruhuna.dcs.cvmanagement.modules.companies.api.dto.response.CompanyResponse;
import lk.ac.ruhuna.dcs.cvmanagement.modules.companies.persistence.entity.CompanyEntity;
import org.springframework.stereotype.Component;

/** Maps Company persistence state to the public API response. */
@Component
public class CompanyMapper {

    public CompanyResponse toResponse(CompanyEntity entity) {
        return new CompanyResponse(
                entity.getId(),
                entity.getName(),
                entity.getWebsiteUrl(),
                entity.getContactPerson(),
                entity.getContactEmail(),
                entity.getContactPhone(),
                entity.getNotes(),
                entity.getVersion() == null ? 0L : entity.getVersion(),
                entity.getCreatedAt(),
                entity.getUpdatedAt());
    }
}
