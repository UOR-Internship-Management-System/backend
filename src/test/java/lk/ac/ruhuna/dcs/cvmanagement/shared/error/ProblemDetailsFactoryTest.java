package lk.ac.ruhuna.dcs.cvmanagement.shared.error;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Map;
import lk.ac.ruhuna.dcs.cvmanagement.shared.http.CorrelationIdContext;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockHttpServletRequest;

class ProblemDetailsFactoryTest {

    private final ProblemDetailsFactory factory = new ProblemDetailsFactory();

    @Test
    void createsCanonicalPreconditionRequiredProblemDetails() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader(CorrelationIdContext.CORRELATION_ID_HEADER, "problem-123");

        ApiErrorResponse response = factory.create(
                HttpStatus.PRECONDITION_REQUIRED,
                ApiErrorCode.IF_MATCH_REQUIRED,
                "If-Match header is required for this operation.",
                request,
                null,
                Map.of());

        assertThat(response.type()).isEqualTo("https://uor-cv-system/errors/if-match-required");
        assertThat(response.title()).isEqualTo("Precondition required");
        assertThat(response.status()).isEqualTo(428);
        assertThat(response.code()).isEqualTo("IF_MATCH_REQUIRED");
        assertThat(response.message()).isEqualTo("If-Match header is required for this operation.");
        assertThat(response.correlationId()).isEqualTo("problem-123");
        assertThat(response.fieldErrors()).isNull();
        assertThat(response.details()).isNull();
    }

    @Test
    void preservesFieldErrorsAndDetailsAsImmutableSnapshots() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        ApiFieldError fieldError = new ApiFieldError("name", "INVALID_VALUE", "Name is required.");

        ApiErrorResponse response = factory.create(
                HttpStatus.BAD_REQUEST,
                ApiErrorCode.VALIDATION_FAILED,
                "One or more request values are invalid.",
                request,
                List.of(fieldError),
                Map.of("source", "request"));

        assertThat(response.fieldErrors()).containsExactly(fieldError);
        assertThat(response.details()).containsEntry("source", "request");
    }
}
