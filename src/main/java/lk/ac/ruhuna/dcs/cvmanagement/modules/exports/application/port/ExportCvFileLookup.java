package lk.ac.ruhuna.dcs.cvmanagement.modules.exports.application.port;

import java.util.Optional;
import java.util.UUID;

/** Export-owned boundary for resolving a candidate's authoritative latest saved CV. */
public interface ExportCvFileLookup {
    Optional<ExportCvFile> findLatestSaved(UUID studentId);

    record ExportCvFile(String fileName, byte[] bytes) {
        public ExportCvFile {
            bytes = bytes.clone();
        }

        @Override
        public byte[] bytes() {
            return bytes.clone();
        }
    }
}
