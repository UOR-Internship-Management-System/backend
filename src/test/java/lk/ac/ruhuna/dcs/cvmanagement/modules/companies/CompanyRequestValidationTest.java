package lk.ac.ruhuna.dcs.cvmanagement.modules.companies;

import static org.assertj.core.api.Assertions.assertThat;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import lk.ac.ruhuna.dcs.cvmanagement.modules.companies.api.dto.request.CompanyRequest;
import org.junit.jupiter.api.Test;

class CompanyRequestValidationTest {

    private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    @Test
    void createContractRejectsBlankNameAndInvalidEmail() {
        CompanyRequest request = new CompanyRequest(
                " ",
                null,
                null,
                "not-an-email",
                null,
                null);

        var fields = validator.validate(request).stream()
                .map(violation -> violation.getPropertyPath().toString())
                .collect(java.util.stream.Collectors.toSet());

        assertThat(fields).contains("name", "contactEmail");
    }

    @Test
    void optionalContactMetadataMayBeNull() {
        CompanyRequest request = new CompanyRequest(
                "Example Technologies",
                null,
                null,
                null,
                null,
                null);

        assertThat(validator.validate(request)).isEmpty();
    }
}
