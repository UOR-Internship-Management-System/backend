package lk.ac.ruhuna.dcs.cvmanagement.modules.cv;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
import lk.ac.ruhuna.dcs.cvmanagement.infrastructure.latex.LatexCvRenderer;
import lk.ac.ruhuna.dcs.cvmanagement.infrastructure.latex.LatexProperties;
import lk.ac.ruhuna.dcs.cvmanagement.infrastructure.latex.PdfGenerationService;
import lk.ac.ruhuna.dcs.cvmanagement.modules.cv.domain.model.CvConfiguration;
import lk.ac.ruhuna.dcs.cvmanagement.modules.cv.domain.model.CvDocumentModel;
import org.junit.jupiter.api.Test;

/**
 * Opt-in real-process acceptance test. Run with -Dcv.latex.integration=true in an environment that has XeLaTeX.
 */
class CvLatexRuntimeAcceptanceTest {

    @Test
    void realXeLatexProducesNonEmptySearchablePdfCandidate() {
        assumeTrue(Boolean.getBoolean("cv.latex.integration"),
                "Enable with -Dcv.latex.integration=true when XeLaTeX is installed.");

        CvDocumentModel model = model();
        String latex = new LatexCvRenderer().render(model);
        byte[] pdf = new PdfGenerationService(new LatexProperties(
                System.getProperty("cv.latex.command", "xelatex"), Duration.ofSeconds(15), 5 * 1024 * 1024, 1))
                .compile(latex);

        assertThat(pdf).hasSizeGreaterThan(1024);
        assertThat(new String(pdf, 0, 5, java.nio.charset.StandardCharsets.US_ASCII)).isEqualTo("%PDF-");
    }

    private CvDocumentModel model() {
        UUID studentId = UUID.fromString("60000000-0000-4000-8000-000000000001");
        OffsetDateTime updatedAt = OffsetDateTime.parse("2026-08-19T05:00:00Z");
        return new CvDocumentModel(
                new CvDocumentModel.Identity(studentId, "Nimal Perera", "nimal@ruh.ac.lk", updatedAt),
                new CvDocumentModel.Profile(UUID.randomUUID(), "Nimal Perera", null,
                        "Software Engineering Student", "Backend developer with Java & PostgreSQL experience.",
                        "+94 77 123 4567", "Matara, Sri Lanka", 1L, updatedAt),
                List.of(),
                List.of(new CvDocumentModel.DeclaredSkill(
                        UUID.randomUUID(), UUID.randomUUID(), "Java", "ADVANCED", 1, 1L, updatedAt)),
                List.of(new CvDocumentModel.Experience(UUID.randomUUID(), "Example Labs", "Software Intern",
                        "Matara", LocalDate.of(2026, 1, 1), null, true,
                        "Built REST APIs with Spring Boot.", 1L, updatedAt)),
                List.of(), List.of(), List.of(), List.of(),
                new CvDocumentModel.AcademicSummary(
                        new BigDecimal("3.52"), new BigDecimal("72"), updatedAt, UUID.randomUUID()),
                new CvConfiguration(List.of(), List.of(), List.of(), List.of(), List.of()));
    }
}
