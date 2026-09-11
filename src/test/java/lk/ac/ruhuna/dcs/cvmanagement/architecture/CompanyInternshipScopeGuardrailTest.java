package lk.ac.ruhuna.dcs.cvmanagement.architecture;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.lang.reflect.RecordComponent;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;
import lk.ac.ruhuna.dcs.cvmanagement.modules.companies.api.dto.request.CompanyRequest;
import lk.ac.ruhuna.dcs.cvmanagement.modules.companies.api.dto.request.CompanyUpdateRequest;
import lk.ac.ruhuna.dcs.cvmanagement.modules.companies.api.dto.response.CompanyResponse;
import lk.ac.ruhuna.dcs.cvmanagement.modules.companies.persistence.entity.CompanyEntity;
import lk.ac.ruhuna.dcs.cvmanagement.modules.internships.api.dto.request.InternshipRequestCreateRequest;
import lk.ac.ruhuna.dcs.cvmanagement.modules.internships.api.dto.request.InternshipRequestUpdateRequest;
import lk.ac.ruhuna.dcs.cvmanagement.modules.internships.api.dto.response.InternshipRequestResponse;
import lk.ac.ruhuna.dcs.cvmanagement.modules.internships.persistence.entity.InternshipRequestEntity;
import org.junit.jupiter.api.Test;

/** Fast, Docker-independent guardrails for the explicitly reduced Company/Internship scope. */
class CompanyInternshipScopeGuardrailTest {

    private static final Path PROJECT_ROOT = Path.of("").toAbsolutePath();
    private static final Path INTERNSHIP_MODULE = PROJECT_ROOT.resolve(
            "src/main/java/lk/ac/ruhuna/dcs/cvmanagement/modules/internships");

    @Test
    void companyContractAndPersistenceDoNotReintroduceLifecycleState() {
        assertThat(recordComponents(CompanyRequest.class)).doesNotContain("active", "status", "deletedAt");
        assertThat(recordComponents(CompanyResponse.class)).doesNotContain("active", "status", "deletedAt");
        assertThat(fieldNames(CompanyUpdateRequest.class)).doesNotContain("active", "status", "deletedAt");
        assertThat(fieldNames(CompanyEntity.class)).doesNotContain("active", "status", "deletedAt");
    }

    @Test
    void internshipContractAndPersistenceDoNotReintroduceRequestStatusOrGpaCriteria() {
        Set<String> forbiddenFields = Set.of(
                "status",
                "minimumGpa",
                "maximumGpa",
                "requiredGpa",
                "gpaRange",
                "gpaEligibility",
                "location",
                "workMode",
                "requestNotes",
                "requiredCompetencyLevel");

        assertThat(recordComponents(InternshipRequestCreateRequest.class)).doesNotContainAnyElementsOf(forbiddenFields);
        assertThat(recordComponents(InternshipRequestResponse.class)).doesNotContainAnyElementsOf(forbiddenFields);
        assertThat(fieldNames(InternshipRequestUpdateRequest.class)).doesNotContainAnyElementsOf(forbiddenFields);
        assertThat(fieldNames(InternshipRequestEntity.class)).doesNotContainAnyElementsOf(forbiddenFields);
    }

    @Test
    void obsoleteInternshipLifecycleClassAndConstantsAreAbsentFromImplementation() throws IOException {
        assertThat(INTERNSHIP_MODULE.resolve("domain/policy/InternshipRequestStatus.java")).doesNotExist();

        String source = readJavaSources(INTERNSHIP_MODULE).toUpperCase(Locale.ROOT);
        assertThat(source).doesNotContain("\"CANCELLED\"", "'CANCELLED'");
        assertThat(source).doesNotContain("\"DRAFT\"", "'DRAFT'");
        assertThat(source).doesNotContain("\"CLOSED\"", "'CLOSED'");
        // ACTIVE is intentionally not forbidden because canonical taxonomy skills still use ACTIVE/INACTIVE.
    }

    @Test
    void companyInternshipMigrationsRemainFreeOfRemovedColumns() throws IOException {
        assertMigrationOmits("V056__create_companies.sql", List.of(" active ", " status ", "deleted_at"));
        assertMigrationOmits(
                "V057__create_internship_requests.sql",
                List.of(
                        " status ",
                        "minimum_gpa",
                        "maximum_gpa",
                        "required_gpa",
                        "gpa_range",
                        "location",
                        "work_mode",
                        "request_notes",
                        "required_competency_level"));
        assertMigrationOmits(
                "V058__create_internship_request_skills.sql",
                List.of("required_competency_level"));
    }

    private Set<String> recordComponents(Class<?> type) {
        return Arrays.stream(type.getRecordComponents())
                .map(RecordComponent::getName)
                .collect(Collectors.toSet());
    }

    private Set<String> fieldNames(Class<?> type) {
        return Arrays.stream(type.getDeclaredFields())
                .map(field -> field.getName())
                .collect(Collectors.toSet());
    }

    private String readJavaSources(Path root) throws IOException {
        StringBuilder content = new StringBuilder();
        try (var paths = Files.walk(root)) {
            for (Path path : paths.filter(Files::isRegularFile)
                    .filter(candidate -> candidate.toString().endsWith(".java"))
                    .toList()) {
                content.append(Files.readString(path, StandardCharsets.UTF_8)).append('\n');
            }
        }
        return content.toString();
    }

    private void assertMigrationOmits(String fileName, List<String> forbiddenTokens) throws IOException {
        Path migration = PROJECT_ROOT.resolve("src/main/resources/db/migration").resolve(fileName);
        String sql = Files.readString(migration, StandardCharsets.UTF_8).lines()
                .map(line -> line.contains("--") ? line.substring(0, line.indexOf("--")) : line)
                .collect(Collectors.joining("\n"))
                .toLowerCase(Locale.ROOT);
        sql = " " + sql + " ";
        for (String forbiddenToken : forbiddenTokens) {
            assertThat(sql)
                    .as("%s must not contain removed column/token %s", fileName, forbiddenToken.trim())
                    .doesNotContain(forbiddenToken.toLowerCase(Locale.ROOT));
        }
    }
}
