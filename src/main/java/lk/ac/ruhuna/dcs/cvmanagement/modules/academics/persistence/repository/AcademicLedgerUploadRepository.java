package lk.ac.ruhuna.dcs.cvmanagement.modules.academics.persistence.repository;

import jakarta.persistence.LockModeType;
import java.util.Collection;
import java.util.Optional;
import java.util.UUID;
import lk.ac.ruhuna.dcs.cvmanagement.modules.academics.domain.AcademicLedgerUploadStatus;
import lk.ac.ruhuna.dcs.cvmanagement.modules.academics.persistence.entity.AcademicLedgerUploadEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface AcademicLedgerUploadRepository extends JpaRepository<AcademicLedgerUploadEntity, UUID> {

    boolean existsByFileHashAndUploadStatusIn(
            String fileHash, Collection<AcademicLedgerUploadStatus> uploadStatuses);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select u from AcademicLedgerUploadEntity u where u.id = :uploadId")
    Optional<AcademicLedgerUploadEntity> findByIdForUpdate(@Param("uploadId") UUID uploadId);
}
