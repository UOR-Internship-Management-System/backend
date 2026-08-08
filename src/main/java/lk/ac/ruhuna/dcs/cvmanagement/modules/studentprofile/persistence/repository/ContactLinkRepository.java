package lk.ac.ruhuna.dcs.cvmanagement.modules.studentprofile.persistence.repository;

import java.util.List;
import java.util.UUID;
import lk.ac.ruhuna.dcs.cvmanagement.modules.studentprofile.persistence.entity.ContactLinkEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ContactLinkRepository extends JpaRepository<ContactLinkEntity, UUID> {
    List<ContactLinkEntity> findByStudentIdOrderByDisplayOrderAsc(UUID studentId);
    long countByStudentId(UUID studentId);
}
