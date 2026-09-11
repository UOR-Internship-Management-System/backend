package lk.ac.ruhuna.dcs.cvmanagement.shared.http;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

class CorrelationIdContextTest {

    @AfterEach
    void clearRequestContext() {
        RequestContextHolder.resetRequestAttributes();
    }

    @Test
    void preservesSafeCorrelationIdAndCachesItOnTheRequest() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader(CorrelationIdContext.CORRELATION_ID_HEADER, "req-123:api");

        String first = CorrelationIdContext.ensure(request);
        request.removeHeader(CorrelationIdContext.CORRELATION_ID_HEADER);
        String second = CorrelationIdContext.ensure(request);

        assertThat(first).isEqualTo("req-123:api");
        assertThat(second).isEqualTo(first);
        assertThat(request.getAttribute(CorrelationIdContext.REQUEST_ATTRIBUTE)).isEqualTo(first);
    }

    @Test
    void fallsBackToRequestIdHeader() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader(CorrelationIdContext.REQUEST_ID_HEADER, "request_456");

        assertThat(CorrelationIdContext.ensure(request)).isEqualTo("request_456");
    }

    @Test
    void replacesUnsafeCorrelationIdWithServerGeneratedUuid() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader(CorrelationIdContext.CORRELATION_ID_HEADER, "unsafe value with spaces");

        String correlationId = CorrelationIdContext.ensure(request);

        assertThat(correlationId).isNotEqualTo("unsafe value with spaces");
        assertThat(UUID.fromString(correlationId)).isNotNull();
    }

    @Test
    void exposesCurrentRequestCorrelationId() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader(CorrelationIdContext.CORRELATION_ID_HEADER, "current-789");
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));

        assertThat(CorrelationIdContext.current()).contains("current-789");
    }

    @Test
    void returnsEmptyOutsideHttpRequestContext() {
        RequestContextHolder.resetRequestAttributes();

        assertThat(CorrelationIdContext.current()).isEmpty();
    }
}
