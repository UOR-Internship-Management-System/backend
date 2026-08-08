package lk.ac.ruhuna.dcs.cvmanagement.modules.studentprofile.domain.policy;

import java.util.UUID;

public interface CvFreshnessUpdatePort {
    void markStale(UUID studentId, String sourceModule);
}
