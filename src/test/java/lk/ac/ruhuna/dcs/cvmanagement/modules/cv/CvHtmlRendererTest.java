package lk.ac.ruhuna.dcs.cvmanagement.modules.cv;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
import lk.ac.ruhuna.dcs.cvmanagement.modules.cv.application.CvHtmlRenderer;
import lk.ac.ruhuna.dcs.cvmanagement.modules.cv.domain.model.CvConfiguration;
import lk.ac.ruhuna.dcs.cvmanagement.modules.cv.domain.model.CvDocumentModel;
import org.junit.jupiter.api.Test;

class CvHtmlRendererTest {

    private final CvHtmlRenderer renderer = new CvHtmlRenderer();

    @Test
    void escapesSourceContentAndUsesCanonicalSectionOrder() {
        OffsetDateTime now = OffsetDateTime.parse("2026-08-19T02:00:00Z");
        CvDocumentModel model = new CvDocumentModel(
                new CvDocumentModel.Identity(UUID.randomUUID(), "<script>alert(1)</script>", "student@ruh.ac.lk", now),
                new CvDocumentModel.Profile(UUID.randomUUID(), null, null, null, "Summary & focus", null, null, 1L, now),
                List.of(new CvDocumentModel.ContactLink(UUID.randomUUID(), "Portfolio", "javascript:alert(1)", 1, 1L, now)),
                List.of(new CvDocumentModel.DeclaredSkill(UUID.randomUUID(), UUID.randomUUID(), "Java", "ADVANCED", 1, 1L, now)),
                List.of(new CvDocumentModel.Experience(UUID.randomUUID(), "Acme", "Intern", null,
                        LocalDate.of(2026, 1, 1), null, true, "Backend", 1L, now)),
                List.of(new CvDocumentModel.Project(UUID.randomUUID(), "Project", "Description", null, null,
                        null, null, 1L, now, List.of())),
                List.of(new CvDocumentModel.Certificate(UUID.randomUUID(), "Certificate", "Issuer", null, null, 1L, now)),
                List.of(new CvDocumentModel.Award(UUID.randomUUID(), "Award", "Issuer", null, null, 1L, now)),
                List.of(new CvDocumentModel.Activity(UUID.randomUUID(), "Club", "Member", null, null, null, 1L, now)),
                null,
                new CvConfiguration(List.of(), List.of(), List.of(), List.of(), List.of()));

        String html = renderer.render(model);

        assertThat(html).contains("&lt;script&gt;alert(1)&lt;/script&gt;").doesNotContain("<script>");
        assertThat(html).contains("Summary &amp; focus");
        assertThat(html).contains("Portfolio").doesNotContain("javascript:");
        assertThat(html).doesNotContain("Academic Summary");
        assertThat(html.indexOf("Professional Summary")).isLessThan(html.indexOf("Skills"));
        assertThat(html.indexOf("Skills")).isLessThan(html.indexOf("Work Experience"));
        assertThat(html.indexOf("Work Experience")).isLessThan(html.indexOf("Projects"));
        assertThat(html.indexOf("Projects")).isLessThan(html.indexOf("Certificates"));
        assertThat(html.indexOf("Certificates")).isLessThan(html.indexOf("Awards and Honors"));
        assertThat(html.indexOf("Awards and Honors")).isLessThan(html.indexOf("Extracurricular Activities"));
    }
}
