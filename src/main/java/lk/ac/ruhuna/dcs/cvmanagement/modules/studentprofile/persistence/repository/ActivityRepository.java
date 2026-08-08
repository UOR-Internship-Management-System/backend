package lk.ac.ruhuna.dcs.cvmanagement.modules.studentprofile.persistence.repository;

import lk.ac.ruhuna.dcs.cvmanagement.modules.studentprofile.persistence.entity.ActivityEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ActivityRepository extends JpaRepository<ActivityEntity, UUID> {
    List<ActivityEntity> findByStudentIdOrderByCreatedAtDesc(UUID studentId);
}
