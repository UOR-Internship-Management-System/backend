package lk.ac.ruhuna.dcs.cvmanagement.shared.http;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import lk.ac.ruhuna.dcs.cvmanagement.shared.error.ApiErrorCode;
import lk.ac.ruhuna.dcs.cvmanagement.shared.error.BadRequestException;
import lk.ac.ruhuna.dcs.cvmanagement.shared.error.PreconditionRequiredException;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

class IfMatchSupportTest {

    @Test
    void parsesStrongQuotedNonNegativeVersion() {
        assertThat(IfMatchSupport.parseVersion("\"0\"")).isZero();
        assertThat(IfMatchSupport.parseVersion("  \"42\"  ")).isEqualTo(42L);
    }

    @Test
    void missingHeaderIsPreconditionRequired() {
        assertThatThrownBy(() -> IfMatchSupport.parseVersion(null))
                .isInstanceOfSatisfying(PreconditionRequiredException.class, exception -> {
                    assertThat(exception.getStatus()).isEqualTo(HttpStatus.PRECONDITION_REQUIRED);
                    assertThat(exception.getErrorCode()).isEqualTo(ApiErrorCode.IF_MATCH_REQUIRED);
                });

        assertThatThrownBy(() -> IfMatchSupport.parseVersion("   "))
                .isInstanceOf(PreconditionRequiredException.class);
    }

    @Test
    void rejectsMalformedOrWeakEntityTags() {
        for (String value : new String[] {"3", "W/\"3\"", "*", "\"-1\"", "\"abc\"", "\"3\",\"4\""}) {
            assertThatThrownBy(() -> IfMatchSupport.parseVersion(value))
                    .as("If-Match value %s", value)
                    .isInstanceOf(BadRequestException.class);
        }
    }

    @Test
    void rejectsVersionOverflow() {
        assertThatThrownBy(() -> IfMatchSupport.parseVersion("\"9223372036854775808\""))
                .isInstanceOf(BadRequestException.class);
    }

    @Test
    void formatsStrongQuotedVersion() {
        assertThat(IfMatchSupport.formatVersion(0)).isEqualTo("\"0\"");
        assertThat(IfMatchSupport.formatVersion(17)).isEqualTo("\"17\"");
        assertThatThrownBy(() -> IfMatchSupport.formatVersion(-1))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
