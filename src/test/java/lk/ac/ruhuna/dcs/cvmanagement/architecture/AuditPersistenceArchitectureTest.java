package lk.ac.ruhuna.dcs.cvmanagement.architecture;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.Test;

class AuditPersistenceArchitectureTest {

    private static final Path MAIN_JAVA = Path.of("src", "main", "java");

    @Test
    void onlyAuditLogAdapterMayInsertAuditEvents() throws IOException {
        List<Path> writers;
        try (var files = Files.walk(MAIN_JAVA)) {
            writers = files.filter(path -> path.toString().endsWith(".java"))
                    .filter(path -> read(path).contains("INSERT INTO public.audit_events"))
                    .toList();
        }

        assertThat(writers)
                .singleElement()
                .satisfies(path -> assertThat(path.getFileName().toString())
                        .isEqualTo("JdbcAuditEventSink.java"));
    }

    @Test
    void applicationCodeCannotUpdateOrDeleteAuditRows() throws IOException {
        try (var files = Files.walk(MAIN_JAVA)) {
            assertThat(files.filter(path -> path.toString().endsWith(".java"))
                            .filter(path -> {
                                String source = read(path);
                                return source.contains("UPDATE public.audit_events")
                                        || source.contains("DELETE FROM public.audit_events");
                            })
                            .toList())
                    .isEmpty();
        }
    }

    private String read(Path path) {
        try {
            return Files.readString(path);
        } catch (IOException exception) {
            throw new IllegalStateException("Could not inspect " + path, exception);
        }
    }
}
