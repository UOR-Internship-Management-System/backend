package lk.ac.ruhuna.dcs.cvmanagement.modules.studentprofile.persistence.repository;

import lk.ac.ruhuna.dcs.cvmanagement.modules.studentprofile.persistence.entity.WorkExperienceEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface WorkExperienceRepository extends JpaRepository<WorkExperienceEntity, UUID> {
    List<WorkExperienceEntity> findByStudentIdOrderByCreatedAtDesc(UUID studentId);
}
