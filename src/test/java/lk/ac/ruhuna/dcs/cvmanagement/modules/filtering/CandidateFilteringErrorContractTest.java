package lk.ac.ruhuna.dcs.cvmanagement.modules.filtering;

import static org.assertj.core.api.Assertions.assertThat;

import lk.ac.ruhuna.dcs.cvmanagement.modules.filtering.domain.exception.FilterDependencyUnavailableException;
import lk.ac.ruhuna.dcs.cvmanagement.modules.filtering.domain.exception.FilterRunNotFoundException;
import lk.ac.ruhuna.dcs.cvmanagement.modules.filtering.domain.exception.InvalidFilterCriteriaException;
import lk.ac.ruhuna.dcs.cvmanagement.shared.error.ApiErrorCode;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

class CandidateFilteringErrorContractTest {

    @Test
    void exposesStableOpenApiErrorCodesAndStatuses() {
        FilterRunNotFoundException notFound = new FilterRunNotFoundException();
        InvalidFilterCriteriaException invalid = new InvalidFilterCriteriaException("Invalid filtering criteria.");
        FilterDependencyUnavailableException unavailable = new FilterDependencyUnavailableException();

        assertThat(notFound.getErrorCode()).isEqualTo(ApiErrorCode.FILTER_RUN_NOT_FOUND);
        assertThat(notFound.getStatus()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(notFound.getMessage()).isEqualTo("The filtering run does not exist.");

        assertThat(invalid.getErrorCode()).isEqualTo(ApiErrorCode.INVALID_FILTER_CRITERIA);
        assertThat(invalid.getStatus()).isEqualTo(HttpStatus.UNPROCESSABLE_CONTENT);

        assertThat(unavailable.getErrorCode()).isEqualTo(ApiErrorCode.FILTER_DEPENDENCY_UNAVAILABLE);
        assertThat(unavailable.getStatus()).isEqualTo(HttpStatus.SERVICE_UNAVAILABLE);
        assertThat(unavailable.getMessage())
                .isEqualTo("Committed academic or declared-skill data is temporarily unavailable.");
    }
}
