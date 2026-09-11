package lk.ac.ruhuna.dcs.cvmanagement.infrastructure.latex;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.Comparator;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import org.springframework.stereotype.Component;

/** Executes XeLaTeX in an isolated temporary directory with strict resource and output bounds. */
@Component
public class PdfGenerationService {

    private static final int MAX_DIAGNOSTIC_BYTES = 64 * 1024;
    private final LatexProperties properties;
    private final Semaphore permits;

    public PdfGenerationService(LatexProperties properties) {
        this.properties = properties;
        this.permits = new Semaphore(properties.maxConcurrentGenerations(), true);
    }

    public byte[] compile(String latexSource) {
        boolean acquired = false;
        Path workDir = null;
        try {
            acquired = permits.tryAcquire(properties.timeout().toMillis(), TimeUnit.MILLISECONDS);
            if (!acquired) throw new LatexCompilationException("CV generation capacity is temporarily unavailable.");

            workDir = Files.createTempDirectory("cv-latex-");
            Path source = workDir.resolve("cv.tex");
            Files.writeString(source, latexSource, StandardCharsets.UTF_8);

            ProcessBuilder builder = new ProcessBuilder(
                    properties.command(),
                    "-no-shell-escape",
                    "-interaction=nonstopmode",
                    "-halt-on-error",
                    "-file-line-error",
                    "-output-directory=" + workDir.toAbsolutePath(),
                    source.toAbsolutePath().toString());
            builder.directory(workDir.toFile());
            builder.redirectErrorStream(true);

            Process process = builder.start();
            try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
                Future<String> diagnostics = executor.submit(() -> drainDiagnostics(process.getInputStream()));
                boolean finished = process.waitFor(properties.timeout().toMillis(), TimeUnit.MILLISECONDS);
                if (!finished) {
                    terminateProcessTree(process);
                    throw new LatexCompilationException("XeLaTeX exceeded the configured generation timeout.");
                }
                String diagnosticText = diagnostics.get(2, TimeUnit.SECONDS);
                if (process.exitValue() != 0) {
                    throw new LatexCompilationException("XeLaTeX compilation failed: " + summarize(diagnosticText));
                }
            }

            Path pdf = workDir.resolve("cv.pdf");
            if (!Files.isRegularFile(pdf)) throw new LatexCompilationException("XeLaTeX did not produce a PDF.");
            long size = Files.size(pdf);
            if (size < 5 || size > properties.maxOutputBytes()) {
                throw new LatexCompilationException("Generated PDF is outside the configured size bounds.");
            }
            byte[] bytes = Files.readAllBytes(pdf);
            if (bytes.length > properties.maxOutputBytes()
                    || bytes[0] != '%' || bytes[1] != 'P' || bytes[2] != 'D' || bytes[3] != 'F' || bytes[4] != '-') {
                throw new LatexCompilationException("Generated output is not a valid PDF artifact.");
            }
            return bytes;
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new LatexCompilationException("CV generation was interrupted.", exception);
        } catch (LatexCompilationException exception) {
            throw exception;
        } catch (Exception exception) {
            throw new LatexCompilationException("CV PDF generation failed.", exception);
        } finally {
            if (workDir != null) deleteRecursively(workDir);
            if (acquired) permits.release();
        }
    }

    private String drainDiagnostics(InputStream input) throws IOException {
        ByteArrayOutputStream retained = new ByteArrayOutputStream(MAX_DIAGNOSTIC_BYTES);
        byte[] buffer = new byte[8192];
        int read;
        while ((read = input.read(buffer)) != -1) {
            int remaining = MAX_DIAGNOSTIC_BYTES - retained.size();
            if (remaining > 0) retained.write(buffer, 0, Math.min(read, remaining));
        }
        return retained.toString(StandardCharsets.UTF_8);
    }

    private void terminateProcessTree(Process process) {
        process.descendants().forEach(handle -> {
            try { handle.destroyForcibly(); } catch (RuntimeException ignored) { }
        });
        process.destroyForcibly();
        try { process.waitFor(2, TimeUnit.SECONDS); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
    }

    private String summarize(String diagnostics) {
        if (diagnostics == null || diagnostics.isBlank()) return "compiler returned a non-zero exit status";
        String oneLine = diagnostics.replaceAll("[\\r\\n\\t]+", " ").replaceAll("\\s{2,}", " ").trim();
        return oneLine.length() <= 240 ? oneLine : oneLine.substring(0, 240);
    }

    private void deleteRecursively(Path root) {
        try (var paths = Files.walk(root)) {
            paths.sorted(Comparator.reverseOrder()).forEach(path -> {
                try { Files.deleteIfExists(path); } catch (IOException ignored) { }
            });
        } catch (IOException ignored) {
            // Temporary-directory cleanup is best effort; no path is exposed to clients.
        }
    }
}
