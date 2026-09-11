package lk.ac.ruhuna.dcs.cvmanagement.modules.studentprofile.persistence.repository;

import jakarta.persistence.LockModeType;
import java.util.Optional;
import java.util.UUID;
import lk.ac.ruhuna.dcs.cvmanagement.modules.studentprofile.persistence.entity.StudentEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface StudentRepository extends JpaRepository<StudentEntity, UUID> {
    Optional<StudentEntity> findByUserAccountId(UUID userAccountId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select s from StudentEntity s where s.userAccountId = :userAccountId")
    Optional<StudentEntity> findByUserAccountIdForUpdate(@Param("userAccountId") UUID userAccountId);
}
