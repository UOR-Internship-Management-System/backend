package lk.ac.ruhuna.dcs.cvmanagement.modules.studentprofile.persistence.repository;

import java.util.Optional;
import java.util.UUID;
import lk.ac.ruhuna.dcs.cvmanagement.modules.studentprofile.persistence.entity.StudentEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StudentRepository extends JpaRepository<StudentEntity, UUID> {
    Optional<StudentEntity> findByUserAccountId(UUID userAccountId);
}
