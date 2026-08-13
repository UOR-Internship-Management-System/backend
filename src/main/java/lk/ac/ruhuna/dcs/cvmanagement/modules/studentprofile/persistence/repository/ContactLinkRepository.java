package lk.ac.ruhuna.dcs.cvmanagement.modules.studentprofile.persistence.repository;

import java.util.UUID;
import lk.ac.ruhuna.dcs.cvmanagement.modules.studentprofile.persistence.entity.ContactLinkEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ContactLinkRepository extends JpaRepository<ContactLinkEntity, UUID> {

    @Query("""
            SELECT c FROM ContactLinkEntity c
            WHERE c.studentId = :studentId
              AND LOWER(c.label) LIKE :searchPattern
            """)
    Page<ContactLinkEntity> search(
        @Param("studentId") UUID studentId, @Param("searchPattern") String searchPattern, Pageable pageable);
}
