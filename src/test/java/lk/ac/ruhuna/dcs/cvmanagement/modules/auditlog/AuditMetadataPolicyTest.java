package lk.ac.ruhuna.dcs.cvmanagement.modules.auditlog;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.Map;
import lk.ac.ruhuna.dcs.cvmanagement.modules.auditlog.application.AuditMetadataPolicy;
import org.junit.jupiter.api.Test;

class AuditMetadataPolicyTest {

    private final AuditMetadataPolicy policy = new AuditMetadataPolicy();

    @Test
    void acceptsBoundedNonSensitiveMetadata() {
        assertThatCode(() -> policy.validate(Map.of(
                        "affectedCount", 3,
                        "guidanceAcknowledged", true,
                        "context", Map.of("source", "ADMIN_UI"))))
                .doesNotThrowAnyException();
    }

    @Test
    void rejectsSensitiveKeysAtAnyDepthIgnoringCaseAndSeparators() {
        assertThatThrownBy(() -> policy.validate(Map.of(
                        "context", Map.of("authorization_token", "Bearer value"))))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Audit metadata contains a forbidden key.");
    }

    @Test
    void rejectsOversizedAndUnsupportedValues() {
        assertThatThrownBy(() -> policy.validate(Map.of("note", "x".repeat(513))))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Audit metadata contains an oversized string value.");
        assertThatThrownBy(() -> policy.validate(Map.of("object", new Object())))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Audit metadata contains an unsupported value type.");
    }
}

