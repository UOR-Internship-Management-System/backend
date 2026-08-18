package lk.ac.ruhuna.dcs.cvmanagement.modules.projects.application.port;

import java.util.Collection;
import java.util.Map;
import java.util.UUID;

/** Reads normalized skill references without exposing Skills module persistence or API types. */
public interface ProjectSkillLookup {

    Map<UUID, ProjectSkillSummary> findByIds(Collection<UUID> skillIds);
}
