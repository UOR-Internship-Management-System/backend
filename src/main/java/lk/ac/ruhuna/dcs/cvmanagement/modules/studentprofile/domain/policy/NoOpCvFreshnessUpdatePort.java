package lk.ac.ruhuna.dcs.cvmanagement.modules.studentprofile.domain.policy;

import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class NoOpCvFreshnessUpdatePort implements CvFreshnessUpdatePort {

    private static final Logger LOGGER = LoggerFactory.getLogger(NoOpCvFreshnessUpdatePort.class);

    @Override
    public void markStale(UUID studentId, String sourceModule) {
        LOGGER.debug("CV freshness invalidation requested for student {} from {} (no-op until BMD-007).",
            studentId, sourceModule);
    }
}
