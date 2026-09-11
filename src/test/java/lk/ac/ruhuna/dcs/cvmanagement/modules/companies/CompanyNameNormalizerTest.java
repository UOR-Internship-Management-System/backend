package lk.ac.ruhuna.dcs.cvmanagement.modules.companies;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import lk.ac.ruhuna.dcs.cvmanagement.modules.companies.domain.policy.CompanyNameNormalizer;
import lk.ac.ruhuna.dcs.cvmanagement.shared.error.ValidationException;
import org.junit.jupiter.api.Test;

class CompanyNameNormalizerTest {

    @Test
    void collapsesWhitespaceAndProducesCaseInsensitiveDuplicateKey() {
        assertThat(CompanyNameNormalizer.displayName("  Example   Technologies  "))
                .isEqualTo("Example Technologies");
        assertThat(CompanyNameNormalizer.duplicateKey(" EXAMPLE   Technologies "))
                .isEqualTo("example technologies");
    }

    @Test
    void rejectsBlankNames() {
        assertThatThrownBy(() -> CompanyNameNormalizer.displayName("   "))
                .isInstanceOf(ValidationException.class);
    }
}
