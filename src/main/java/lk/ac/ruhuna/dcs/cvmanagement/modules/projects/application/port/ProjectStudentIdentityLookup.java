package lk.ac.ruhuna.dcs.cvmanagement.modules.projects.application.port;

import java.util.Optional;
import java.util.UUID;

/** Resolves the authenticated account to a Student without exposing Student Profile persistence. */
public interface ProjectStudentIdentityLookup {

    Optional<UUID> findStudentIdByUserAccountId(UUID userAccountId);
}
