package lk.ac.ruhuna.dcs.cvmanagement.modules.projects;

import static org.assertj.core.api.Assertions.assertThat;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import lk.ac.ruhuna.dcs.cvmanagement.modules.projects.api.dto.request.ProjectCreateRequest;
import lk.ac.ruhuna.dcs.cvmanagement.modules.projects.api.dto.request.ProjectUpdateRequest;
import org.junit.jupiter.api.Test;

class ProjectRequestValidationTest {

    private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    @Test
    void createRequiresNonBlankBoundedTitle() {
        ProjectCreateRequest blank =
                new ProjectCreateRequest("   ", null, null, null, null, null, List.of(), true);
        ProjectCreateRequest oversized =
                new ProjectCreateRequest("x".repeat(201), null, null, null, null, null, List.of(), true);

        assertThat(validator.validate(blank)).anyMatch(violation -> violation.getPropertyPath().toString()
                .equals("title"));
        assertThat(validator.validate(oversized)).anyMatch(violation -> violation.getPropertyPath().toString()
                .equals("title"));
    }

    @Test
    void createAndUpdateRejectReversedDateRanges() {
        LocalDate start = LocalDate.parse("2026-06-10");
        LocalDate end = LocalDate.parse("2026-06-01");
        ProjectCreateRequest create =
                new ProjectCreateRequest("Portfolio", null, null, null, start, end, List.of(), true);
        ProjectUpdateRequest update =
                new ProjectUpdateRequest(null, null, null, null, start, end, null, null);

        assertThat(validator.validate(create)).anyMatch(violation -> violation.getMessage().contains("End date"));
        assertThat(validator.validate(update)).anyMatch(violation -> violation.getMessage().contains("End date"));
    }

    @Test
    void skillIdentifiersCannotContainNullElements() {
        ProjectCreateRequest request = new ProjectCreateRequest(
                "Portfolio", null, null, null, null, null, Arrays.asList((java.util.UUID) null), true);

        assertThat(validator.validate(request)).anyMatch(violation -> violation.getPropertyPath().toString()
                .contains("skillIds"));
    }
}
