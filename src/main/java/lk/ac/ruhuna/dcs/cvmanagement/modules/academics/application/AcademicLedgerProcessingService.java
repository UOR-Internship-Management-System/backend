package lk.ac.ruhuna.dcs.cvmanagement.modules.academics.application;

import java.io.IOException;
import java.io.InputStream;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.UUID;
import lk.ac.ruhuna.dcs.cvmanagement.infrastructure.storage.FileAssetEntity;
import lk.ac.ruhuna.dcs.cvmanagement.infrastructure.storage.FileAssetRepository;
import lk.ac.ruhuna.dcs.cvmanagement.infrastructure.storage.FileStorageException;
import lk.ac.ruhuna.dcs.cvmanagement.infrastructure.storage.FileStoragePort;
import lk.ac.ruhuna.dcs.cvmanagement.modules.academics.config.AcademicLedgerProcessingProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/** Replays one durably claimed source file into staging and then starts domain validation. */
@Service
class AcademicLedgerProcessingService {

    private static final Logger LOGGER = LoggerFactory.getLogger(AcademicLedgerProcessingService.class);

    private final FileAssetRepository fileAssetRepository;
    private final FileStoragePort fileStorage;
    private final AcademicLedgerSourceParser sourceParser;
    private final AcademicLedgerStagingWriter stagingWriter;
    private final AcademicLedgerProcessingStateService stateService;
    private final AcademicLedgerValidationService validationService;
    private final AcademicLedgerProcessingProperties properties;

    AcademicLedgerProcessingService(
            FileAssetRepository fileAssetRepository,
            FileStoragePort fileStorage,
            AcademicLedgerSourceParser sourceParser,
            AcademicLedgerStagingWriter stagingWriter,
            AcademicLedgerProcessingStateService stateService,
            AcademicLedgerValidationService validationService,
            AcademicLedgerProcessingProperties properties) {
        this.fileAssetRepository = fileAssetRepository;
        this.fileStorage = fileStorage;
        this.sourceParser = sourceParser;
        this.stagingWriter = stagingWriter;
        this.stateService = stateService;
        this.validationService = validationService;
        this.properties = properties;
    }

    void process(AcademicLedgerProcessingStateService.ProcessingJob job) {
        UUID uploadId = job.uploadId();
        try {
            // A claimed RECEIVED batch must not inherit rows from an interrupted prior processing attempt.
            stateService.clearStaging(uploadId);
            FileAssetEntity asset = fileAssetRepository.findById(job.fileAssetId())
                    .orElseThrow(() -> new AcademicLedgerProcessingException("Source file metadata is unavailable."));
            int totalRows;
            MessageDigest digest = sha256();
            try (InputStream storedInput = fileStorage.open(asset.getStorageKey());
                    DigestInputStream input = new DigestInputStream(storedInput, digest)) {
                totalRows = sourceParser.parse(input, properties.stagingBatchSize(), rows -> {
                    stagingWriter.write(uploadId, rows);
                    stateService.heartbeatProcessing(uploadId);
                });
            }
            String actualChecksum = HexFormat.of().formatHex(digest.digest());
            if (!actualChecksum.equals(asset.getChecksumSha256())) {
                throw new AcademicLedgerProcessingException("Stored Academic Ledger source checksum does not match metadata.");
            }
            if (totalRows == 0) {
                throw new AcademicLedgerProcessingException("The accepted CSV contains no academic data rows.");
            }
            stateService.markStaged(uploadId, totalRows);
            validationService.validate(uploadId);
        } catch (AcademicLedgerProcessingException | FileStorageException | IOException exception) {
            LOGGER.warn("Academic Ledger upload {} could not be processed: {}", uploadId, exception.getMessage());
            stateService.markProcessingFailed(uploadId, exception.getMessage());
        } catch (RuntimeException exception) {
            LOGGER.error("Unexpected Academic Ledger processing failure for upload {}.", uploadId, exception);
            stateService.markProcessingFailed(uploadId, "Academic Ledger processing failed unexpectedly.");
        }
    }

    private MessageDigest sha256() {
        try {
            return MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 is not available in this JVM.", exception);
        }
    }
}
