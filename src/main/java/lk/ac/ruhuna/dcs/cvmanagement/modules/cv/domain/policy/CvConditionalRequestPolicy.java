package lk.ac.ruhuna.dcs.cvmanagement.modules.cv.domain.policy;

import lk.ac.ruhuna.dcs.cvmanagement.modules.cv.domain.exception.CvPreconditionRequiredException;
import lk.ac.ruhuna.dcs.cvmanagement.modules.cv.domain.exception.StaleCvException;
import lk.ac.ruhuna.dcs.cvmanagement.shared.error.BadRequestException;
import org.springframework.stereotype.Component;

/** Enforces the OpenAPI first-create/replacement conditional request contract. */
@Component
public class CvConditionalRequestPolicy {

    public void validate(boolean activeCvExists, int currentRevision, Long ifMatchRevision, String ifNoneMatch) {
        if (ifNoneMatch != null && !ifNoneMatch.isBlank() && !"*".equals(ifNoneMatch.trim())) {
            throw new BadRequestException("If-None-Match must be * when creating the first saved CV.");
        }
        boolean noneMatchStar = "*".equals(ifNoneMatch == null ? null : ifNoneMatch.trim());
        if (ifMatchRevision != null && noneMatchStar) {
            throw new BadRequestException("If-Match and If-None-Match cannot be supplied together.");
        }

        if (!activeCvExists) {
            if (ifMatchRevision != null) throw new StaleCvException();
            if (!noneMatchStar) throw new CvPreconditionRequiredException();
            return;
        }

        if (noneMatchStar) throw new StaleCvException();
        if (ifMatchRevision == null) throw new CvPreconditionRequiredException();
        if (ifMatchRevision != currentRevision) throw new StaleCvException();
    }
}
