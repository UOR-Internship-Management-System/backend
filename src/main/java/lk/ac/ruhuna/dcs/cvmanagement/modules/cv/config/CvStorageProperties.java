package lk.ac.ruhuna.dcs.cvmanagement.modules.cv.config;

import java.nio.file.Path;
import org.springframework.boot.context.properties.ConfigurationProperties;

/** Private durable storage root for staged and active generated CV PDFs. */
@ConfigurationProperties(prefix = "app.cv.storage")
public record CvStorageProperties(Path root) {
    public CvStorageProperties {
        if (root == null) {
            throw new IllegalArgumentException("app.cv.storage.root must be configured.");
        }
    }
}
