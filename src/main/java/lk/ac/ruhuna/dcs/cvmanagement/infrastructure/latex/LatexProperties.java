package lk.ac.ruhuna.dcs.cvmanagement.infrastructure.latex;

import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;

/** Bounded external-process settings for backend-controlled XeLaTeX compilation. */
@ConfigurationProperties(prefix = "app.cv.latex")
public record LatexProperties(
        String command,
        Duration timeout,
        long maxOutputBytes,
        int maxConcurrentGenerations) {

    public LatexProperties {
        command = command == null || command.isBlank() ? "xelatex" : command.trim();
        timeout = timeout == null ? Duration.ofSeconds(10) : timeout;
        if (timeout.isZero() || timeout.isNegative() || timeout.compareTo(Duration.ofMinutes(1)) > 0) {
            throw new IllegalArgumentException("app.cv.latex.timeout must be between 1 ms and 1 minute.");
        }
        if (maxOutputBytes == 0) maxOutputBytes = 5L * 1024L * 1024L;
        if (maxConcurrentGenerations == 0) maxConcurrentGenerations = 2;
        if (maxOutputBytes < 1024 || maxOutputBytes > 50L * 1024L * 1024L) {
            throw new IllegalArgumentException("app.cv.latex.max-output-bytes must be between 1 KiB and 50 MiB.");
        }
        if (maxConcurrentGenerations < 1 || maxConcurrentGenerations > 16) {
            throw new IllegalArgumentException("app.cv.latex.max-concurrent-generations must be between 1 and 16.");
        }
    }
}
