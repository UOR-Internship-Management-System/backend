package lk.ac.ruhuna.dcs.cvmanagement.modules.cv;

import static org.assertj.core.api.Assertions.assertThat;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lk.ac.ruhuna.dcs.cvmanagement.modules.cv.api.dto.request.CvPreviewRequest;
import org.junit.jupiter.api.Test;

class CvPreviewRequestValidationTest {

    private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    @Test
    void requiresAllFiveSelectionArrays() {
        CvPreviewRequest request = new CvPreviewRequest(null, List.of(), List.of(), List.of(), List.of());

        assertThat(validator.validate(request))
                .anyMatch(violation -> violation.getPropertyPath().toString().equals("includedExperienceIds"));
    }

    @Test
    void rejectsDuplicateSelectedRecordIds() {
        UUID id = UUID.randomUUID();
        CvPreviewRequest request = new CvPreviewRequest(List.of(id, id), List.of(), List.of(), List.of(), List.of());

        assertThat(validator.validate(request))
                .anyMatch(violation -> violation.getMessage().contains("unique"));
    }

    @Test
    void rejectsMoreThanOneHundredIdsPerSelectionGroup() {
        List<UUID> ids = new ArrayList<>();
        for (int i = 0; i < 101; i++) {
            ids.add(UUID.randomUUID());
        }
        CvPreviewRequest request = new CvPreviewRequest(ids, List.of(), List.of(), List.of(), List.of());

        assertThat(validator.validate(request))
                .anyMatch(violation -> violation.getPropertyPath().toString().equals("includedExperienceIds"));
    }
}
