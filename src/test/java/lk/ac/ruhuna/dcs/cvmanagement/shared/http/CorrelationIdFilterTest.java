package lk.ac.ruhuna.dcs.cvmanagement.shared.http;

import static org.assertj.core.api.Assertions.assertThat;

import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

class CorrelationIdFilterTest {

    private final CorrelationIdFilter filter = new CorrelationIdFilter();

    @Test
    void establishesCorrelationIdBeforeContinuingFilterChain() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader(CorrelationIdContext.CORRELATION_ID_HEADER, "correlation-123");
        MockHttpServletResponse response = new MockHttpServletResponse();
        String[] observed = new String[1];
        FilterChain chain = (servletRequest, servletResponse) ->
                observed[0] = (String) servletRequest.getAttribute(CorrelationIdContext.REQUEST_ATTRIBUTE);

        filter.doFilter(request, response, chain);

        assertThat(observed[0]).isEqualTo("correlation-123");
        assertThat(response.getHeader(CorrelationIdContext.CORRELATION_ID_HEADER)).isEqualTo("correlation-123");
    }
}
