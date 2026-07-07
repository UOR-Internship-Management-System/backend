package lk.ac.ruhuna.dcs.cvmanagement.architecture;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Locale;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;

class RemovedScopeGuardrailTest {

    private static final List<String> FORBIDDEN_TOKENS = List.of(
            "temporary_password",
            "temp_password",
            "admin_approval",
            "registration_approval",
            "pending_registration",
            "rejected_registration",
            "skill_master",
            "verified_skill",
            "company_login",
            "ai_score",
            "ai_ranking",
            "match_percentage",
            "cv_review",
            "cv_approval",
            "project_approval",
            "gpa_required");

    @Test
    void implementationArtifactsDoNotContainRemovedScopeTokens() throws IOException {
        Path root = Path.of("").toAbsolutePath();
        List<Path> scanRoots = List.of(
                root.resolve("src/main/java"),
                root.resolve("src/main/resources/db/migration"),
                root.resolve("src/main/resources/application.yml"),
                root.resolve("src/main/resources/application-local.yml"),
                root.resolve("src/main/resources/application-test.yml"),
                root.resolve("docker"),
                root.resolve("scripts"),
                root.resolve(".github/workflows"));

        List<String> violations = scanRoots.stream()
                .filter(Files::exists)
                .flatMap(this::walkRegularFiles)
                .flatMap(path -> findForbiddenTokens(path).stream())
                .toList();

        assertThat(violations).isEmpty();
    }

    private Stream<Path> walkRegularFiles(Path path) {
        try {
            if (Files.isRegularFile(path)) {
                return Stream.of(path);
            }
            return Files.walk(path).filter(Files::isRegularFile);
        } catch (IOException exception) {
            throw new IllegalStateException("Unable to scan " + path, exception);
        }
    }

    private List<String> findForbiddenTokens(Path path) {
        try {
            String content = Files.readString(path, StandardCharsets.UTF_8).toLowerCase(Locale.ROOT);
            return FORBIDDEN_TOKENS.stream()
                    .filter(content::contains)
                    .map(token -> path + " contains " + token)
                    .toList();
        } catch (IOException exception) {
            throw new IllegalStateException("Unable to read " + path, exception);
        }
    }
}
