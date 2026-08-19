package lk.ac.ruhuna.dcs.cvmanagement.modules.cv;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;

/** Guardrail for the finalized BMD-007 surface in the bundled OpenAPI v1.6.0 resource. */
class CvOpenApiContractTest {

    @Test
    void bundledOpenApiKeepsSingleActiveCvContractAndRemovedScopeOut() throws IOException {
        String yaml;
        try (var input = new ClassPathResource("openapi/CV_Management_API_OpenAPI_v1.6.0.yaml").getInputStream()) {
            yaml = new String(input.readAllBytes(), StandardCharsets.UTF_8);
        }

        assertThat(yaml)
                .contains("/me/cv/source-freshness:")
                .contains("/me/cv/preview:")
                .contains("/me/cv:")
                .contains("/me/cv/download:")
                .contains("/admin/students/{studentId}/latest-cv:")
                .contains("/admin/students/{studentId}/latest-cv/download:")
                .contains("If-None-Match")
                .contains("If-Match")
                .contains("PRECONDITION_REQUIRED")
                .contains("STALE_VERSION")
                .contains("CV_PREVIEW_EXPIRED")
                .contains("CV_FILE_UNAVAILABLE")
                .doesNotContain("/me/cv/versions")
                .doesNotContain("latexSource")
                .doesNotContain("CV_APPROVED")
                .doesNotContain("CV_REJECTED");
    }
}
