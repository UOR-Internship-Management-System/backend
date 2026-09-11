package lk.ac.ruhuna.dcs.cvmanagement.modules.studentprofile;

import static org.assertj.core.api.Assertions.assertThat;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import java.time.LocalDate;
import lk.ac.ruhuna.dcs.cvmanagement.modules.studentprofile.api.dto.request.ActivityRequest;
import lk.ac.ruhuna.dcs.cvmanagement.modules.studentprofile.api.dto.request.AwardRequest;
import lk.ac.ruhuna.dcs.cvmanagement.modules.studentprofile.api.dto.request.CertificateRequest;
import lk.ac.ruhuna.dcs.cvmanagement.modules.studentprofile.api.dto.request.WorkExperienceRequest;
import org.junit.jupiter.api.Test;

class StudentCvSupportingRequestValidationTest {

    private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    @Test
    void requiresCanonicalCertificateAndAwardFields() {
        assertThat(validator.validate(new CertificateRequest("Certificate", " ", null, null, true))).hasSize(2);
        assertThat(validator.validate(new AwardRequest("Award", null, null, null, true))).hasSize(2);
    }

    @Test
    void requiresActivityRoleAndValidDateRange() {
        ActivityRequest request = new ActivityRequest(
                "Engineering Society",
                " ",
                LocalDate.of(2026, 6, 1),
                LocalDate.of(2026, 5, 1),
                null,
                true);

        assertThat(validator.validate(request)).hasSize(2);
    }

    @Test
    void enforcesExperienceRequiredFieldsAndCurrentRoleDateRules() {
        WorkExperienceRequest currentRoleWithEndDate = new WorkExperienceRequest(
                "Company",
                "Engineer",
                null,
                LocalDate.of(2026, 1, 1),
                LocalDate.of(2026, 2, 1),
                true,
                null,
                true);
        WorkExperienceRequest pastRoleWithoutEndDate = new WorkExperienceRequest(
                "Company",
                "Engineer",
                null,
                LocalDate.of(2026, 1, 1),
                null,
                false,
                null,
                true);

        assertThat(validator.validate(currentRoleWithEndDate)).hasSize(1);
        assertThat(validator.validate(pastRoleWithoutEndDate)).hasSize(1);
    }
}
