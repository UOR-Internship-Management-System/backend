package lk.ac.ruhuna.dcs.cvmanagement.modules.shortlists.persistence.repository;

import jakarta.persistence.LockModeType;
import java.util.Optional;
import java.util.UUID;
import lk.ac.ruhuna.dcs.cvmanagement.modules.shortlists.persistence.entity.ShortlistEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/** Mutation and locking boundary for shortlists. */
public interface ShortlistRepository extends JpaRepository<ShortlistEntity, UUID> {

    boolean existsByInternshipRequestId(UUID internshipRequestId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select s from ShortlistEntity s where s.id = :id")
    Optional<ShortlistEntity> findByIdForUpdate(@Param("id") UUID id);
}
