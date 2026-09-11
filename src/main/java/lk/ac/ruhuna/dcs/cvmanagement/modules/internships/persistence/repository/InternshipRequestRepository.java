package lk.ac.ruhuna.dcs.cvmanagement.modules.internships.persistence.repository;

import java.util.UUID;
import lk.ac.ruhuna.dcs.cvmanagement.modules.internships.persistence.entity.InternshipRequestEntity;
import org.springframework.data.jpa.repository.JpaRepository;

/** Mutation/direct-lookup persistence boundary for Internship Requests. */
public interface InternshipRequestRepository extends JpaRepository<InternshipRequestEntity, UUID> {
}
