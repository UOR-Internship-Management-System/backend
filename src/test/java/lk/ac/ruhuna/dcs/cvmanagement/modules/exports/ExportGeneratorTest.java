package lk.ac.ruhuna.dcs.cvmanagement.modules.exports;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.zip.ZipFile;
import lk.ac.ruhuna.dcs.cvmanagement.modules.exports.application.port.ExportCvFileLookup;
import lk.ac.ruhuna.dcs.cvmanagement.modules.exports.application.BulkCvZipExportGenerator;
import lk.ac.ruhuna.dcs.cvmanagement.modules.exports.application.ShortlistCsvExportGenerator;
import lk.ac.ruhuna.dcs.cvmanagement.modules.exports.persistence.query.ExportReadRepository.ExportCandidate;
import lk.ac.ruhuna.dcs.cvmanagement.modules.exports.persistence.query.ExportReadRepository.ExportShortlist;
import org.junit.jupiter.api.Test;

class ExportGeneratorTest {

    private final UUID shortlistId = UUID.fromString("d1000000-0000-4000-8000-000000000001");
    private final UUID firstStudentId = UUID.fromString("d1000000-0000-4000-8000-000000000002");
    private final UUID secondStudentId = UUID.fromString("d1000000-0000-4000-8000-000000000003");
    private final ExportShortlist shortlist = new ExportShortlist(
            shortlistId, true, "Primary", "Acme", "Backend Intern");

    @Test
    void csvEscapesFormulaAndCommaBearingFields() throws Exception {
        ExportCandidate candidate = candidate(firstStudentId, "SC/2022/12865", "=Doe, Jane");
        try (var generated = new ShortlistCsvExportGenerator().generate(shortlist, List.of(candidate))) {
            String csv = Files.readString(generated.path(), StandardCharsets.UTF_8);
            assertThat(csv).contains("\"'=Doe, Jane\"");
            assertThat(generated.contentType()).isEqualTo("text/csv");
            assertThat(generated.totalCandidateCount()).isEqualTo(1);
        }
    }

    @Test
    void bulkZipIncludesAvailableCvAndReportsMissingCandidate() throws Exception {
        ExportCvFileLookup resolver = mock(ExportCvFileLookup.class);
        when(resolver.findLatestSaved(firstStudentId)).thenReturn(Optional.of(
                new ExportCvFileLookup.ExportCvFile("cv.pdf", new byte[] {1, 2, 3, 4})));
        when(resolver.findLatestSaved(secondStudentId)).thenReturn(Optional.empty());

        try (var generated = new BulkCvZipExportGenerator(resolver).generate(
                shortlist,
                List.of(candidate(firstStudentId, "SC/2022/12865", "Jane"),
                        candidate(secondStudentId, "SC/2022/12866", "John")))) {
            assertThat(generated.includedFileCount()).isEqualTo(1);
            assertThat(generated.missingCvCandidates()).extracting(ExportCandidate::studentId)
                    .containsExactly(secondStudentId);
            try (ZipFile zip = new ZipFile(generated.path().toFile())) {
                assertThat(zip.getEntry("SC-2022-12865.pdf")).isNotNull();
            }
        }
    }

    private ExportCandidate candidate(UUID studentId, String indexNumber, String name) {
        return new ExportCandidate(
                studentId, indexNumber, name, new BigDecimal("3.42"), OffsetDateTime.parse("2026-08-22T03:00:00Z"));
    }
}
