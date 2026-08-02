package lk.ac.ruhuna.dcs.cvmanagement.modules.studentprofile.persistence.repository;

import lk.ac.ruhuna.dcs.cvmanagement.modules.studentprofile.persistence.entity.CertificateEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface CertificateRepository extends JpaRepository<CertificateEntity, UUID> {
    List<CertificateEntity> findByStudentIdOrderByCreatedAtDesc(UUID studentId);
}
