package lk.ac.ruhuna.dcs.cvmanagement.modules.studentprofile.persistence.repository;

import lk.ac.ruhuna.dcs.cvmanagement.modules.studentprofile.persistence.entity.AwardEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface AwardRepository extends JpaRepository<AwardEntity, UUID> {
    List<AwardEntity> findByStudentIdOrderByCreatedAtDesc(UUID studentId);
}
