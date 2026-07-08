package lk.ac.ruhuna.dcs.cvmanagement.architecture;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.Test;

/**
 * Verifies that the expected Sprint 1 package structure exists on disk.
 * <p>Uses filesystem assertions rather than reflection to keep dependencies minimal.
 */
class PackageStructureTest {

    private static final Path ROOT = resolveSourceRoot();

    private static final String BASE_PACKAGE_PATH =
            "lk/ac/ruhuna/dcs/cvmanagement";

    private static final List<String> TOP_LEVEL_PACKAGES = List.of(
            "config",
            "infrastructure",
            "modules",
            "shared");

    private static final List<String> MODULE_PACKAGES = List.of(
            "academics",
            "adminstudents",
            "auditlog",
            "auth",
            "companies",
            "cv",
            "exports",
            "filtering",
            "health",
            "internships",
            "projects",
            "shortlists",
            "skills",
            "studentprofile",
            "verification");

    private static final List<String> CORE_MODULE_LAYERS = List.of(
            "api",
            "application",
            "domain",
            "mapper",
            "persistence");

    private static final List<String> MODULES_WITH_FULL_LAYERS = List.of(
            "auth",
            "verification");

    @Test
    void rootPackageDirectoryExists() {
        assertThat(ROOT.resolve(BASE_PACKAGE_PATH))
                .as("Root package directory")
                .isDirectory();
    }

    @Test
    void topLevelPackagesExist() {
        for (String pkg : TOP_LEVEL_PACKAGES) {
            assertThat(ROOT.resolve(BASE_PACKAGE_PATH).resolve(pkg))
                    .as("Top-level package: " + pkg)
                    .isDirectory();
        }
    }

    @Test
    void allModulePackagesExist() {
        Path modulesDir = ROOT.resolve(BASE_PACKAGE_PATH).resolve("modules");
        for (String module : MODULE_PACKAGES) {
            assertThat(modulesDir.resolve(module))
                    .as("Module package: " + module)
                    .isDirectory();
        }
    }

    @Test
    void healthModuleHasApiLayer() {
        Path healthDir = ROOT.resolve(BASE_PACKAGE_PATH).resolve("modules/health/api");
        assertThat(healthDir)
                .as("Health module api layer")
                .isDirectory();
    }

    @Test
    void coreModulesHaveExpectedLayers() {
        Path modulesDir = ROOT.resolve(BASE_PACKAGE_PATH).resolve("modules");
        for (String module : MODULES_WITH_FULL_LAYERS) {
            for (String layer : CORE_MODULE_LAYERS) {
                assertThat(modulesDir.resolve(module).resolve(layer))
                        .as("Module %s layer %s", module, layer)
                        .isDirectory();
            }
        }
    }

    @Test
    void mainApplicationClassExists() {
        assertThat(ROOT.resolve(BASE_PACKAGE_PATH).resolve("CvManagementApplication.java"))
                .as("Main application class")
                .isRegularFile();
    }

    private static Path resolveSourceRoot() {
        Path projectRoot = Path.of("").toAbsolutePath();
        Path srcMain = projectRoot.resolve("src/main/java");
        if (Files.isDirectory(srcMain)) {
            return srcMain;
        }
        throw new IllegalStateException("Cannot find src/main/java from " + projectRoot);
    }
}
