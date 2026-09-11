package lk.ac.ruhuna.dcs.cvmanagement.shared.cv;

import java.util.UUID;

/**
 * Narrow cross-module boundary used by Student source modules to invalidate CV freshness.
 * Implementations must participate in the caller's transaction.
 */
public interface CvSourceFreshnessUpdatePort {
    void markChanged(UUID studentId, CvSourceArea sourceArea);
}
