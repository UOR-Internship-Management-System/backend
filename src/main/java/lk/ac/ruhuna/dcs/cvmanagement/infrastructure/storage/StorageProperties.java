package lk.ac.ruhuna.dcs.cvmanagement.infrastructure.storage;

import java.nio.file.Path;
import org.springframework.boot.context.properties.ConfigurationProperties;

/** Configuration for the local durable-file adapter used by Academic Ledger uploads. */
@ConfigurationProperties(prefix = "app.academics.storage")
public record StorageProperties(Path root) {

    public StorageProperties {
        if (root == null) {
            throw new IllegalArgumentException("app.academics.storage.root must be configured.");
        }
    }
}
