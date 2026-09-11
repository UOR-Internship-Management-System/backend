package lk.ac.ruhuna.dcs.cvmanagement.modules.companies.persistence.repository;

import java.util.UUID;
import lk.ac.ruhuna.dcs.cvmanagement.modules.companies.persistence.entity.CompanyEntity;
import org.springframework.data.jpa.repository.JpaRepository;

/** Spring Data persistence boundary for Company metadata mutations and direct lookups. */
public interface CompanyRepository extends JpaRepository<CompanyEntity, UUID> {

    boolean existsByNormalizedName(String normalizedName);

    boolean existsByNormalizedNameAndIdNot(String normalizedName, UUID companyId);
}
