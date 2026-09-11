package lk.ac.ruhuna.dcs.cvmanagement.modules.exports.application;

import java.util.List;
import lk.ac.ruhuna.dcs.cvmanagement.modules.exports.config.ExportProperties;
import lk.ac.ruhuna.dcs.cvmanagement.modules.exports.domain.policy.ExportType;
import lk.ac.ruhuna.dcs.cvmanagement.modules.exports.persistence.query.ExportReadRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/** Claims queued export jobs and produces durable artifacts outside the claim transaction. */
@Component
public class ExportProcessingWorker {
    private static final Logger LOGGER = LoggerFactory.getLogger(ExportProcessingWorker.class);
    private final ExportJobService jobService;
    private final ExportReadRepository readRepository;
    private final ShortlistCsvExportGenerator csvGenerator;
    private final BulkCvZipExportGenerator zipGenerator;
    private final ExportProperties properties;

    public ExportProcessingWorker(
            ExportJobService jobService,
            ExportReadRepository readRepository,
            ShortlistCsvExportGenerator csvGenerator,
            BulkCvZipExportGenerator zipGenerator,
            ExportProperties properties) {
        this.jobService = jobService;
        this.readRepository = readRepository;
        this.csvGenerator = csvGenerator;
        this.zipGenerator = zipGenerator;
        this.properties = properties;
    }

    @Scheduled(fixedDelayString = "${app.exports.processing.worker-poll-delay-ms:2000}")
    public void processQueuedJobs() {
        if (!properties.processing().workerEnabled()) {
            return;
        }
        for (int index = 0; index < properties.processing().maxJobsPerPoll(); index++) {
            var claimed = jobService.claimNext();
            if (claimed.isEmpty()) {
                return;
            }
            process(claimed.get());
        }
    }

    private void process(ExportJobSnapshot job) {
        try {
            var shortlist = readRepository.findShortlist(job.shortlistId())
                    .orElseThrow(() -> new ExportGenerationException(
                            "SHORTLIST_NOT_FOUND", "The shortlist no longer exists."));
            var candidates = List.copyOf(readRepository.findCandidates(job.shortlistId()));
            try (GeneratedExport generated = job.exportType() == ExportType.SHORTLIST_SUMMARY_CSV
                    ? csvGenerator.generate(shortlist, candidates)
                    : zipGenerator.generate(shortlist, candidates)) {
                String extension = job.exportType() == ExportType.SHORTLIST_SUMMARY_CSV ? ".csv" : ".zip";
                jobService.complete(
                        job.exportJobId(), generated, "exports/" + job.exportJobId() + "/artifact" + extension);
            }
        } catch (ExportGenerationException exception) {
            jobService.fail(job.exportJobId(), exception.code(), exception.getMessage());
        } catch (RuntimeException exception) {
            LOGGER.error("Export job {} failed unexpectedly.", job.exportJobId(), exception);
            jobService.fail(job.exportJobId(), "EXPORT_PROCESSING_FAILED", "Export processing failed.");
        }
    }
}
