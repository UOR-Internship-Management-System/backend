package lk.ac.ruhuna.dcs.cvmanagement.modules.exports.application;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import lk.ac.ruhuna.dcs.cvmanagement.modules.exports.application.port.ExportCvFileLookup;
import lk.ac.ruhuna.dcs.cvmanagement.modules.exports.persistence.query.ExportReadRepository.ExportCandidate;
import lk.ac.ruhuna.dcs.cvmanagement.modules.exports.persistence.query.ExportReadRepository.ExportShortlist;
import org.springframework.stereotype.Component;

/** Generates a ZIP containing each candidate's authoritative latest saved CV. */
@Component
public class BulkCvZipExportGenerator {
    private final ExportCvFileLookup cvFileLookup;

    public BulkCvZipExportGenerator(ExportCvFileLookup cvFileLookup) {
        this.cvFileLookup = cvFileLookup;
    }

    public GeneratedExport generate(ExportShortlist shortlist, List<ExportCandidate> candidates) {
        Path temporary = null;
        List<ExportCandidate> missing = new ArrayList<>();
        Set<String> usedNames = new HashSet<>();
        int included = 0;
        try {
            temporary = Files.createTempFile("shortlist-cvs-", ".zip");
            try (ZipOutputStream zip = new ZipOutputStream(
                    new BufferedOutputStream(Files.newOutputStream(temporary)))) {
                for (ExportCandidate candidate : candidates) {
                    var cv = cvFileLookup.findLatestSaved(candidate.studentId());
                    if (cv.isPresent()) {
                        zip.putNextEntry(new ZipEntry(uniqueEntryName(candidate.indexNumber(), usedNames)));
                        zip.write(cv.get().bytes());
                        zip.closeEntry();
                        included++;
                    } else {
                        missing.add(candidate);
                    }
                }
            }
            if (!candidates.isEmpty() && included == 0) {
                throw new ExportGenerationException(
                        "NO_CV_FILES_AVAILABLE", "No candidate has an available saved CV.");
            }
            return new GeneratedExport(
                    temporary,
                    "shortlist-" + shortlist.shortlistId() + "-cvs.zip",
                    "application/zip",
                    candidates.size(),
                    included,
                    missing);
        } catch (IOException exception) {
            deleteQuietly(temporary);
            throw new ExportGenerationException(
                    "ZIP_GENERATION_FAILED", "Unable to generate the bulk CV archive.", exception);
        } catch (RuntimeException exception) {
            deleteQuietly(temporary);
            throw exception;
        }
    }

    private String uniqueEntryName(String indexNumber, Set<String> usedNames) {
        String prefix = indexNumber == null ? "student" : indexNumber.replaceAll("[^A-Za-z0-9._-]", "-");
        if (prefix.isBlank()) {
            prefix = "student";
        }
        String candidate = prefix + ".pdf";
        int counter = 2;
        while (!usedNames.add(candidate.toLowerCase())) {
            candidate = prefix + "-" + counter++ + ".pdf";
        }
        return candidate;
    }

    private void deleteQuietly(Path path) {
        if (path == null) {
            return;
        }
        try {
            Files.deleteIfExists(path);
        } catch (IOException ignored) {
            // Preserve the generation failure.
        }
    }
}
