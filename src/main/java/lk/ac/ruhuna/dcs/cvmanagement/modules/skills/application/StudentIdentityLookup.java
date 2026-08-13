package lk.ac.ruhuna.dcs.cvmanagement.modules.skills.application;

import java.util.Optional;
import java.util.UUID;

/** Provides student identity data without exposing another module's persistence layer. */
public interface StudentIdentityLookup {

    Optional<UUID> findStudentIdByUserAccountId(UUID userAccountId);
}
