package lk.ac.ruhuna.dcs.cvmanagement.modules.academics.persistence.repository;

import java.util.UUID;
import lk.ac.ruhuna.dcs.cvmanagement.modules.academics.persistence.entity.AcademicLedgerStagingRowEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface AcademicLedgerStagingRowRepository extends JpaRepository<AcademicLedgerStagingRowEntity, UUID> {

    Page<AcademicLedgerStagingRowEntity> findByAcademicLedgerUploadId(
            UUID academicLedgerUploadId, Pageable pageable);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("delete from AcademicLedgerStagingRowEntity r where r.academicLedgerUploadId = :uploadId")
    int deleteAllByUploadId(@Param("uploadId") UUID uploadId);
}
