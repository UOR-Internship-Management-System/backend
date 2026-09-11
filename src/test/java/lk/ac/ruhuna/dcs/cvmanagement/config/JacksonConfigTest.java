package lk.ac.ruhuna.dcs.cvmanagement.config;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.OffsetDateTime;
import org.junit.jupiter.api.Test;

class JacksonConfigTest {

    private final ObjectMapper objectMapper = new JacksonConfig().objectMapper();

    @Test
    void serializesOffsetDateTimeAsIso8601Text() throws Exception {
        OffsetDateTime value = OffsetDateTime.parse("2026-08-21T10:15:30+05:30");

        assertThat(objectMapper.writeValueAsString(value))
                .isEqualTo("\"2026-08-21T10:15:30+05:30\"");
    }
}
