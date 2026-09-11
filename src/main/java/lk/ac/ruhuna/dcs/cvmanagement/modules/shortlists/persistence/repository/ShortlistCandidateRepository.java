package lk.ac.ruhuna.dcs.cvmanagement.modules.shortlists.persistence.repository;

import java.util.Collection;
import java.util.Optional;
import java.util.List;
import java.util.UUID;
import lk.ac.ruhuna.dcs.cvmanagement.modules.shortlists.persistence.entity.ShortlistCandidateEntity;
import org.springframework.data.jpa.repository.JpaRepository;

/** Mutation/direct-count boundary for shortlist membership. */
public interface ShortlistCandidateRepository extends JpaRepository<ShortlistCandidateEntity, UUID> {

    long countByShortlistId(UUID shortlistId);

    List<ShortlistCandidateEntity> findAllByShortlistIdAndStudentIdIn(
            UUID shortlistId,
            Collection<UUID> studentIds);

    Optional<ShortlistCandidateEntity> findByShortlistIdAndStudentId(UUID shortlistId, UUID studentId);

    long deleteByShortlistIdAndStudentId(UUID shortlistId, UUID studentId);
}
