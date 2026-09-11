package lk.ac.ruhuna.dcs.cvmanagement.modules.exports.application;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import lk.ac.ruhuna.dcs.cvmanagement.modules.exports.persistence.query.ExportReadRepository.ExportCandidate;

/** Temporary generated artifact and its immutable processing outcome. */
public record GeneratedExport(
        Path path,
        String fileName,
        String contentType,
        int totalCandidateCount,
        int includedFileCount,
        List<ExportCandidate> missingCvCandidates) implements AutoCloseable {
    public GeneratedExport { missingCvCandidates = List.copyOf(missingCvCandidates); }
    @Override public void close() {
        try { Files.deleteIfExists(path); } catch (IOException ignored) { }
    }
}
