package lk.ac.ruhuna.dcs.cvmanagement.modules.internships.persistence.repository;

import java.util.Optional;
import java.util.UUID;
import lk.ac.ruhuna.dcs.cvmanagement.modules.internships.persistence.entity.InternshipRequestSkillEntity;
import org.springframework.data.jpa.repository.JpaRepository;

/** Mutation/direct-lookup persistence boundary for request-skill associations. */
public interface InternshipRequestSkillRepository extends JpaRepository<InternshipRequestSkillEntity, UUID> {

    boolean existsByInternshipRequestIdAndSkillId(UUID internshipRequestId, UUID skillId);

    Optional<InternshipRequestSkillEntity> findByIdAndInternshipRequestId(UUID id, UUID internshipRequestId);

    void deleteAllByInternshipRequestId(UUID internshipRequestId);
}
