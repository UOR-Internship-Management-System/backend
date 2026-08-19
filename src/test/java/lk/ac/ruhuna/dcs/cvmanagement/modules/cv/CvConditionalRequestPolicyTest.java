package lk.ac.ruhuna.dcs.cvmanagement.modules.cv;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import lk.ac.ruhuna.dcs.cvmanagement.modules.cv.domain.exception.CvPreconditionRequiredException;
import lk.ac.ruhuna.dcs.cvmanagement.modules.cv.domain.exception.StaleCvException;
import lk.ac.ruhuna.dcs.cvmanagement.modules.cv.domain.policy.CvConditionalRequestPolicy;
import lk.ac.ruhuna.dcs.cvmanagement.shared.error.BadRequestException;
import org.junit.jupiter.api.Test;

class CvConditionalRequestPolicyTest {
    private final CvConditionalRequestPolicy policy = new CvConditionalRequestPolicy();

    @Test
    void firstSaveRequiresIfNoneMatchStar() {
        assertThatCode(() -> policy.validate(false, 0, null, "*")).doesNotThrowAnyException();
        assertThatThrownBy(() -> policy.validate(false, 0, null, null))
                .isInstanceOf(CvPreconditionRequiredException.class);
        assertThatThrownBy(() -> policy.validate(false, 0, 1L, null))
                .isInstanceOf(StaleCvException.class);
    }

    @Test
    void replacementRequiresMatchingRevision() {
        assertThatCode(() -> policy.validate(true, 4, 4L, null)).doesNotThrowAnyException();
        assertThatThrownBy(() -> policy.validate(true, 4, null, null))
                .isInstanceOf(CvPreconditionRequiredException.class);
        assertThatThrownBy(() -> policy.validate(true, 4, 3L, null))
                .isInstanceOf(StaleCvException.class);
        assertThatThrownBy(() -> policy.validate(true, 4, null, "*"))
                .isInstanceOf(StaleCvException.class);
    }

    @Test
    void rejectsContradictoryOrMalformedConditionalHeaders() {
        assertThatThrownBy(() -> policy.validate(true, 1, 1L, "*"))
                .isInstanceOf(BadRequestException.class);
        assertThatThrownBy(() -> policy.validate(false, 0, null, "\"anything\""))
                .isInstanceOf(BadRequestException.class);
    }
}
