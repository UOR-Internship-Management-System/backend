package lk.ac.ruhuna.dcs.cvmanagement.modules.cv.application;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.DistributionSummary;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import lk.ac.ruhuna.dcs.cvmanagement.infrastructure.latex.LatexCompilationException;
import lk.ac.ruhuna.dcs.cvmanagement.infrastructure.latex.LatexCvRenderer;
import lk.ac.ruhuna.dcs.cvmanagement.infrastructure.latex.PdfGenerationService;
import lk.ac.ruhuna.dcs.cvmanagement.modules.cv.domain.exception.CvGenerationFailedException;
import lk.ac.ruhuna.dcs.cvmanagement.modules.cv.domain.model.CvDocumentModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/** Generates the exact backend-controlled PDF candidate associated with a CV preview. */
@Service
public class CvGenerationService {

    private static final Logger LOGGER = LoggerFactory.getLogger(CvGenerationService.class);
    private final LatexCvRenderer latexRenderer;
    private final PdfGenerationService pdfGenerationService;
    private final Timer generationTimer;
    private final Counter generationFailureCounter;
    private final DistributionSummary generatedPdfSize;

    public CvGenerationService(
            LatexCvRenderer latexRenderer,
            PdfGenerationService pdfGenerationService,
            MeterRegistry meterRegistry) {
        this.latexRenderer = latexRenderer;
        this.pdfGenerationService = pdfGenerationService;
        this.generationTimer = meterRegistry.timer("cv.generation.duration");
        this.generationFailureCounter = meterRegistry.counter("cv.generation.failures");
        this.generatedPdfSize = DistributionSummary.builder("cv.generation.pdf.size.bytes")
                .baseUnit("bytes")
                .register(meterRegistry);
    }

    public byte[] generatePdf(CvDocumentModel document) {
        try {
            return generationTimer.record(() -> {
                byte[] pdf = pdfGenerationService.compile(latexRenderer.render(document));
                generatedPdfSize.record(pdf.length);
                return pdf;
            });
        } catch (LatexCompilationException exception) {
            generationFailureCounter.increment();
            LOGGER.warn("CV PDF generation failed.");
            throw new CvGenerationFailedException();
        }
    }
}
