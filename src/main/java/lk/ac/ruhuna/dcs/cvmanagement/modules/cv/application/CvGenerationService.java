package lk.ac.ruhuna.dcs.cvmanagement.modules.cv.application;

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

    public CvGenerationService(LatexCvRenderer latexRenderer, PdfGenerationService pdfGenerationService) {
        this.latexRenderer = latexRenderer;
        this.pdfGenerationService = pdfGenerationService;
    }

    public byte[] generatePdf(CvDocumentModel document) {
        try {
            return pdfGenerationService.compile(latexRenderer.render(document));
        } catch (LatexCompilationException exception) {
            LOGGER.warn("CV PDF generation failed.");
            throw new CvGenerationFailedException();
        }
    }
}
