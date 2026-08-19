package lk.ac.ruhuna.dcs.cvmanagement.modules.cv;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
import lk.ac.ruhuna.dcs.cvmanagement.infrastructure.latex.LatexCvRenderer;
import lk.ac.ruhuna.dcs.cvmanagement.modules.cv.domain.model.CvConfiguration;
import lk.ac.ruhuna.dcs.cvmanagement.modules.cv.domain.model.CvDocumentModel;
import org.junit.jupiter.api.Test;

class LatexCvRendererTest {

    private final LatexCvRenderer renderer = new LatexCvRenderer();

    @Test
    void escapesTexMetacharactersAndKeepsFixedAtsSectionOrder() {
        UUID studentId = UUID.randomUUID();
        OffsetDateTime now = OffsetDateTime.parse("2026-08-19T02:00:00Z");
        var model = new CvDocumentModel(
                new CvDocumentModel.Identity(studentId, "A&B_Student #1", "student@dcs.ruh.ac.lk", now),
                new CvDocumentModel.Profile(UUID.randomUUID(), null, null, "Java % Developer", "Uses {Spring} & SQL", null, null, 1L, now),
                List.of(),
                List.of(new CvDocumentModel.DeclaredSkill(UUID.randomUUID(), UUID.randomUUID(), "Java", "ADVANCED", 1, 1L, now)),
                List.of(), List.of(), List.of(), List.of(), List.of(), null,
                new CvConfiguration(List.of(), List.of(), List.of(), List.of(), List.of()));

        String latex = renderer.render(model);

        assertThat(latex).contains("A\\&B\\_Student \\#1");
        assertThat(latex).contains("Java \\% Developer");
        assertThat(latex).contains("Uses \\{Spring\\} \\& SQL");
        assertThat(latex).contains("\\cvsection{Professional Summary}");
        assertThat(latex).contains("\\cvsection{Skills}");
        assertThat(latex.indexOf("Professional Summary")).isLessThan(latex.indexOf("Skills"));
        assertThat(latex).doesNotContain("\\write18");
    }

    @Test
    void escapesAllControlMetacharactersUsedBySourceText() {
        assertThat(renderer.escape("\\{}$&#%_~^"))
                .isEqualTo("\\textbackslash{}\\{\\}\\$\\&\\#\\%\\_\\textasciitilde{}\\textasciicircum{}");
    }
}
