package lk.ac.ruhuna.dcs.cvmanagement.modules.cv;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import lk.ac.ruhuna.dcs.cvmanagement.infrastructure.latex.LatexCompilationException;
import lk.ac.ruhuna.dcs.cvmanagement.infrastructure.latex.LatexCvRenderer;
import lk.ac.ruhuna.dcs.cvmanagement.infrastructure.latex.PdfGenerationService;
import lk.ac.ruhuna.dcs.cvmanagement.modules.cv.application.CvGenerationService;
import lk.ac.ruhuna.dcs.cvmanagement.modules.cv.domain.exception.CvGenerationFailedException;
import lk.ac.ruhuna.dcs.cvmanagement.modules.cv.domain.model.CvDocumentModel;
import org.junit.jupiter.api.Test;

class CvGenerationServiceTest {

    @Test
    void recordsDurationAndPdfSizeWithoutHighCardinalityTags() {
        LatexCvRenderer renderer = mock(LatexCvRenderer.class);
        PdfGenerationService pdfs = mock(PdfGenerationService.class);
        CvDocumentModel document = mock(CvDocumentModel.class);
        SimpleMeterRegistry registry = new SimpleMeterRegistry();
        byte[] bytes = "%PDF-1.7\nbody".getBytes(java.nio.charset.StandardCharsets.US_ASCII);
        when(renderer.render(document)).thenReturn("latex");
        when(pdfs.compile("latex")).thenReturn(bytes);

        byte[] result = new CvGenerationService(renderer, pdfs, registry).generatePdf(document);

        assertThat(result).isEqualTo(bytes);
        assertThat(registry.timer("cv.generation.duration").count()).isEqualTo(1);
        assertThat(registry.summary("cv.generation.pdf.size.bytes").count()).isEqualTo(1);
        assertThat(registry.summary("cv.generation.pdf.size.bytes").totalAmount()).isEqualTo(bytes.length);
        assertThat(registry.counter("cv.generation.failures").count()).isZero();
    }

    @Test
    void compilerFailureIncrementsFailureMetricAndMapsToPublicCvError() {
        LatexCvRenderer renderer = mock(LatexCvRenderer.class);
        PdfGenerationService pdfs = mock(PdfGenerationService.class);
        CvDocumentModel document = mock(CvDocumentModel.class);
        SimpleMeterRegistry registry = new SimpleMeterRegistry();
        when(renderer.render(document)).thenReturn("latex");
        when(pdfs.compile("latex")).thenThrow(new LatexCompilationException("internal compiler detail"));

        assertThatThrownBy(() -> new CvGenerationService(renderer, pdfs, registry).generatePdf(document))
                .isInstanceOf(CvGenerationFailedException.class);

        assertThat(registry.counter("cv.generation.failures").count()).isEqualTo(1);
        assertThat(registry.timer("cv.generation.duration").count()).isEqualTo(1);
    }
}
