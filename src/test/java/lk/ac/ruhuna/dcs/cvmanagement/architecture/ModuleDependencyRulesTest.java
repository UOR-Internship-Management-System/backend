package lk.ac.ruhuna.dcs.cvmanagement.architecture;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;

/**
 * Verifies module dependency rules via source-file scanning.
 * <p>Uses lightweight regex-based import scanning rather than ArchUnit to keep
 * Sprint 1 dependencies minimal.
 */
class ModuleDependencyRulesTest {

    private static final Path PROJECT_ROOT = Path.of("").toAbsolutePath();
    private static final Path SRC_MAIN_JAVA = PROJECT_ROOT.resolve("src/main/java");

    private static final String ROOT_PACKAGE = "lk.ac.ruhuna.dcs.cvmanagement";
    private static final String MODULES_PACKAGE = ROOT_PACKAGE + ".modules";

    /** Known module names under modules/. */
    private static final List<String> MODULE_NAMES = List.of(
            "academics", "admindashboard", "adminstudents", "auditlog", "auth", "companies",
            "cv", "exports", "filtering", "health", "internships",
            "projects", "shortlists", "skills", "studentprofile", "verification");

    /** Packages a module is allowed to import from (besides its own). */
    private static final Set<String> ALLOWED_IMPORT_PREFIXES = Set.of(
            "java.",
            "javax.",
            "jakarta.",
            "org.springframework.",
            "org.flywaydb.",
            "lombok.",
            ROOT_PACKAGE + ".shared.",
            ROOT_PACKAGE + ".infrastructure.",
            ROOT_PACKAGE + ".config.");

    private static final Set<String> SPRINT_2_ALLOWED_MODULE_IMPORTS = Set.of(
            "auth->verification",
            "academics->studentprofile",
            "cv->studentprofile",
            "cv->academics",
            "cv->projects",
            "cv->skills",
            "adminstudents->cv");

    /** Annotation patterns that would expose accidental endpoints. */
    private static final Pattern ENDPOINT_ANNOTATION = Pattern.compile(
            "@(GetMapping|PostMapping|PutMapping|PatchMapping|DeleteMapping|RequestMapping)");

    private static final List<String> REMOVED_SCOPE_TOKENS = List.of(
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
    void allMainJavaFilesAreUnderRootPackage() throws IOException {
        String rootPath = ROOT_PACKAGE.replace('.', '/');
        List<String> violations = new ArrayList<>();

        try (Stream<Path> files = Files.walk(SRC_MAIN_JAVA)) {
            files.filter(p -> p.toString().endsWith(".java"))
                    .filter(Files::isRegularFile)
                    .forEach(path -> {
                        String relative = SRC_MAIN_JAVA.relativize(path).toString().replace('\\', '/');
                        if (!relative.startsWith(rootPath)) {
                            violations.add(relative + " is outside root package " + ROOT_PACKAGE);
                        }
                    });
        }

        assertThat(violations)
                .as("All main Java files should be under " + ROOT_PACKAGE)
                .isEmpty();
    }

    @Test
    void moduleCodeDoesNotImportFromOtherModules() throws IOException {
        List<String> violations = new ArrayList<>();

        for (String moduleName : MODULE_NAMES) {
            Path moduleDir = SRC_MAIN_JAVA.resolve(
                    MODULES_PACKAGE.replace('.', '/') + "/" + moduleName);
            if (!Files.isDirectory(moduleDir)) {
                continue;
            }

            String ownModulePackage = MODULES_PACKAGE + "." + moduleName;

            try (Stream<Path> files = Files.walk(moduleDir)) {
                files.filter(p -> p.toString().endsWith(".java"))
                        .filter(Files::isRegularFile)
                        .forEach(path -> {
                            try {
                                List<String> lines = Files.readAllLines(path, StandardCharsets.UTF_8);
                                for (String line : lines) {
                                    if (!line.startsWith("import ")) {
                                        continue;
                                    }
                                    String imported = line.substring(7).replace(";", "").trim();
                                    if (imported.startsWith("static ")) {
                                        imported = imported.substring(7).trim();
                                    }

                                    // Skip if importing from own module
                                    if (imported.startsWith(ownModulePackage)) {
                                        continue;
                                    }

                                    // Skip if importing from allowed prefixes
                                    boolean allowed = ALLOWED_IMPORT_PREFIXES.stream()
                                            .anyMatch(imported::startsWith);
                                    if (allowed) {
                                        continue;
                                    }

                                    // Check if importing from another module
                                    if (imported.startsWith(MODULES_PACKAGE + ".")) {
                                        String importedModule = imported.substring((MODULES_PACKAGE + ".").length())
                                                .split("\\.")[0];
                                        if (SPRINT_2_ALLOWED_MODULE_IMPORTS.contains(moduleName + "->" + importedModule)) {
                                            continue;
                                        }
                                        violations.add(path.getFileName() + " in module '"
                                                + moduleName + "' imports cross-module: " + imported);
                                    }
                                }
                            } catch (IOException e) {
                                throw new IllegalStateException("Cannot read " + path, e);
                            }
                        });
            }
        }

        assertThat(violations)
                .as("Module code must not import from other modules")
                .isEmpty();
    }

    @Test
    void adminStudentsUsesOnlyCvApplicationPortsAcrossModuleBoundary() throws IOException {
        Path moduleDir = SRC_MAIN_JAVA.resolve(MODULES_PACKAGE.replace('.', '/') + "/adminstudents");
        String allowedPrefix = MODULES_PACKAGE + ".cv.application.port.";
        String cvPrefix = MODULES_PACKAGE + ".cv.";
        List<String> violations = new ArrayList<>();

        try (Stream<Path> files = Files.walk(moduleDir)) {
            files.filter(p -> p.toString().endsWith(".java"))
                    .filter(Files::isRegularFile)
                    .forEach(path -> {
                        try {
                            for (String line : Files.readAllLines(path, StandardCharsets.UTF_8)) {
                                if (!line.startsWith("import ")) {
                                    continue;
                                }
                                String imported = line.substring(7).replace(";", "").trim();
                                if (imported.startsWith(cvPrefix) && !imported.startsWith(allowedPrefix)) {
                                    violations.add(path.getFileName() + " imports CV implementation detail: " + imported);
                                }
                            }
                        } catch (IOException exception) {
                            throw new IllegalStateException("Cannot read " + path, exception);
                        }
                    });
        }

        assertThat(violations)
                .as("Admin Student Inspection must consume BMD-007 through application ports only")
                .isEmpty();
    }

    @Test
    void futureModuleControllersDoNotExposeEndpoints() throws IOException {
        List<String> violations = new ArrayList<>();

        Set<String> activeModules = Set.of(
                "health", "auth", "verification", "studentprofile", "admindashboard", "skills", "academics",
                "adminstudents", "companies", "internships", "projects", "cv", "filtering", "shortlists");

        for (String moduleName : MODULE_NAMES) {
            if (activeModules.contains(moduleName)) {
                continue;
            }
            Path moduleApiDir = SRC_MAIN_JAVA.resolve(
                    MODULES_PACKAGE.replace('.', '/') + "/" + moduleName + "/api");
            if (!Files.isDirectory(moduleApiDir)) {
                continue;
            }

            try (Stream<Path> files = Files.walk(moduleApiDir)) {
                files.filter(p -> p.toString().endsWith(".java"))
                        .filter(Files::isRegularFile)
                        .forEach(path -> {
                            try {
                                String content = Files.readString(path, StandardCharsets.UTF_8);
                                if (ENDPOINT_ANNOTATION.matcher(content).find()) {
                                            violations.add(path.getFileName()
                                                    + " in future module '" + moduleName
                                            + "' exposes an endpoint annotation before its approved sprint");
                                }
                            } catch (IOException e) {
                                throw new IllegalStateException("Cannot read " + path, e);
                            }
                        });
            }
        }

        assertThat(violations)
                .as("Future module controllers must not expose endpoints in Sprint 1")
                .isEmpty();
    }

    @Test
    void implementationPackagesDoNotContainRemovedScopeTokens() throws IOException {
        List<String> violations = new ArrayList<>();

        try (Stream<Path> files = Files.walk(SRC_MAIN_JAVA)) {
            files.filter(p -> p.toString().endsWith(".java"))
                    .filter(Files::isRegularFile)
                    .forEach(path -> {
                        try {
                            String content = Files.readString(path, StandardCharsets.UTF_8).toLowerCase();
                            for (String token : REMOVED_SCOPE_TOKENS) {
                                if (content.contains(token)) {
                                    violations.add(SRC_MAIN_JAVA.relativize(path) + " contains removed-scope token " + token);
                                }
                            }
                        } catch (IOException e) {
                            throw new IllegalStateException("Cannot read " + path, e);
                        }
                    });
        }

        assertThat(violations)
                .as("Implementation packages must not contain removed-scope class or endpoint names")
                .isEmpty();
    }
}
