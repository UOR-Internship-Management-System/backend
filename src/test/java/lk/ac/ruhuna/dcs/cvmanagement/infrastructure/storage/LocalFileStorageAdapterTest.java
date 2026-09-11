package lk.ac.ruhuna.dcs.cvmanagement.infrastructure.storage;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class LocalFileStorageAdapterTest {

    @TempDir
    Path tempDir;

    @Test
    void storesOpensAndDeletesContentUnderGeneratedKey() throws Exception {
        LocalFileStorageAdapter adapter = new LocalFileStorageAdapter(new StorageProperties(tempDir));
        byte[] content = "academic-ledger".getBytes(StandardCharsets.UTF_8);

        FileStoragePort.StoredFile stored = adapter.store(
                "academic-ledger/2026/08/test.csv", new java.io.ByteArrayInputStream(content));

        assertThat(stored.sizeBytes()).isEqualTo(content.length);
        assertThat(stored.checksumSha256()).hasSize(64);
        try (var input = adapter.open("academic-ledger/2026/08/test.csv")) {
            assertThat(input.readAllBytes()).isEqualTo(content);
        }

        adapter.delete("academic-ledger/2026/08/test.csv");
        assertThat(Files.exists(tempDir.resolve("academic-ledger/2026/08/test.csv"))).isFalse();
    }

    @Test
    void rejectsTraversalStorageKeys() {
        LocalFileStorageAdapter adapter = new LocalFileStorageAdapter(new StorageProperties(tempDir));

        assertThatThrownBy(() -> adapter.store("../escape.csv", new java.io.ByteArrayInputStream(new byte[] {1})))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void refusesToOverwriteAnExistingStorageKey() {
        LocalFileStorageAdapter adapter = new LocalFileStorageAdapter(new StorageProperties(tempDir));
        adapter.store("academic-ledger/existing.csv", new java.io.ByteArrayInputStream(new byte[] {1}));

        assertThatThrownBy(() -> adapter.store(
                        "academic-ledger/existing.csv", new java.io.ByteArrayInputStream(new byte[] {2})))
                .isInstanceOf(FileStorageException.class);
    }
}
