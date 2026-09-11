package lk.ac.ruhuna.dcs.cvmanagement.modules.filtering;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import lk.ac.ruhuna.dcs.cvmanagement.shared.api.ApiPaths;
import org.junit.jupiter.api.Test;

class CandidateFilteringOpenApiContractTest {

    private static final Path OPEN_API = Path.of(
            "src/main/resources/openapi/CV_Management_API_OpenAPI_v1.6.0.yaml");

    @Test
    void controllerBasePathMatchesExecutableOpenApi() throws IOException {
        String yaml = Files.readString(OPEN_API, StandardCharsets.UTF_8);

        assertThat(ApiPaths.ADMIN_CANDIDATE_FILTERING_RUNS)
                .isEqualTo("/api/v1/admin/candidate-filtering/runs");
        assertThat(yaml)
                .contains("  /admin/candidate-filtering/runs:")
                .contains("  /admin/candidate-filtering/runs/{filterRunId}:")
                .contains("  /admin/candidate-filtering/runs/{filterRunId}/candidates:")
                .contains("$ref: '#/components/schemas/CandidateFilteringRunResponse'")
                .contains("$ref: '#/components/schemas/PagedCandidateFilteringCandidateResponse'")
                .contains("$ref: '#/components/responses/FilterDependencyUnavailable503'");
    }

    @Test
    void currentCandidateContractStillRequiresDownstreamFactualEnrichment() throws IOException {
        String yaml = Files.readString(OPEN_API, StandardCharsets.UTF_8);
        int start = yaml.indexOf("    CandidateFilteringCandidateResponse:");
        int end = yaml.indexOf("\n    StudentDashboardMetricsResponse:", start);
        assertThat(start).isGreaterThanOrEqualTo(0);
        assertThat(end).isGreaterThan(start);

        String candidateSchema = yaml.substring(start, end);
        assertThat(candidateSchema)
                .contains("        - hasLatestSavedCv")
                .contains("        - hasExistingActiveShortlist")
                .contains("        - existingActiveShortlistCount");
    }
}
