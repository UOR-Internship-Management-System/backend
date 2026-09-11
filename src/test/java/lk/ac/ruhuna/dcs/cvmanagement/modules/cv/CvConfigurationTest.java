package lk.ac.ruhuna.dcs.cvmanagement.modules.cv;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;
import java.util.UUID;
import lk.ac.ruhuna.dcs.cvmanagement.modules.cv.domain.exception.CvConfigurationInvalidException;
import lk.ac.ruhuna.dcs.cvmanagement.modules.cv.domain.model.CvConfiguration;
import org.junit.jupiter.api.Test;

class CvConfigurationTest {

    @Test
    void canonicalizesSelectionOrderSoClientOrderCannotControlOutput() {
        UUID low = UUID.fromString("00000000-0000-0000-0000-000000000001");
        UUID high = UUID.fromString("ffffffff-ffff-ffff-ffff-ffffffffffff");

        CvConfiguration configuration = new CvConfiguration(
                List.of(high, low), List.of(), List.of(), List.of(), List.of());

        assertThat(configuration.includedExperienceIds()).containsExactly(low, high);
    }

    @Test
    void rejectsDuplicateIdsWhenConstructedOutsideTheHttpValidationBoundary() {
        UUID id = UUID.randomUUID();

        assertThatThrownBy(() -> new CvConfiguration(
                        List.of(id, id), List.of(), List.of(), List.of(), List.of()))
                .isInstanceOf(CvConfigurationInvalidException.class);
    }
}
