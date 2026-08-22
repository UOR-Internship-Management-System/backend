package lk.ac.ruhuna.dcs.cvmanagement.modules.exports.application;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import lk.ac.ruhuna.dcs.cvmanagement.modules.exports.persistence.query.ExportReadRepository.ExportCandidate;
import lk.ac.ruhuna.dcs.cvmanagement.modules.exports.persistence.query.ExportReadRepository.ExportShortlist;
import org.springframework.stereotype.Component;

@Component
public class ShortlistCsvExportGenerator {
    public GeneratedExport generate(ExportShortlist shortlist, List<ExportCandidate> candidates) {
        Path temporary = null;
        try {
            temporary = Files.createTempFile("shortlist-summary-", ".csv");
            try (BufferedWriter writer = Files.newBufferedWriter(temporary, StandardCharsets.UTF_8)) {
                writer.write("shortlistId,shortlistName,companyName,internshipTitle,studentId,indexNumber,fullName,computerScienceGpa,selectedAt");
                writer.newLine();
                for (ExportCandidate candidate : candidates) {
                    writer.write(String.join(",",
                            csv(shortlist.shortlistId().toString()), csv(shortlist.name()),
                            csv(shortlist.companyName()), csv(shortlist.roleTitle()),
                            csv(candidate.studentId().toString()), csv(candidate.indexNumber()),
                            csv(candidate.fullName()), csv(candidate.officialGpa() == null ? null : candidate.officialGpa().toPlainString()),
                            csv(candidate.selectedAt().toString())));
                    writer.newLine();
                }
            }
            return new GeneratedExport(
                    temporary, "shortlist-" + shortlist.shortlistId() + ".csv", "text/csv",
                    candidates.size(), 1, List.of());
        } catch (IOException exception) {
            if (temporary != null) try { Files.deleteIfExists(temporary); } catch (IOException ignored) { }
            throw new ExportGenerationException("CSV_GENERATION_FAILED", "Unable to generate shortlist CSV.", exception);
        }
    }

    private String csv(String value) {
        String safe = value == null ? "" : value;
        if (!safe.isEmpty() && "=+-@\t\r".indexOf(safe.charAt(0)) >= 0) {
            safe = "'" + safe;
        }
        return '"' + safe.replace("\"", "\"\"") + '"';
    }
}
