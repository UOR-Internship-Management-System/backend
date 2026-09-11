package lk.ac.ruhuna.dcs.cvmanagement.modules.filtering.persistence.repository;

import java.util.UUID;
import lk.ac.ruhuna.dcs.cvmanagement.modules.filtering.persistence.entity.FilterRunEntity;
import org.springframework.data.jpa.repository.JpaRepository;

/** Persistence boundary for immutable Candidate Filtering run metadata. */
public interface FilterRunRepository extends JpaRepository<FilterRunEntity, UUID> {
}
