package lk.ac.ruhuna.dcs.cvmanagement.modules.cv.application.port;

import java.util.UUID;

/** Resolves the immutable bytes of the current saved CV without exposing storage keys or paths. */
public interface ActiveCvFileResolver {
    ResolvedCvFile resolve(UUID studentId);

    record ResolvedCvFile(UUID cvId, int revision, String fileName, long fileSizeBytes, byte[] bytes) {
        public ResolvedCvFile {
            bytes = bytes.clone();
        }
        @Override public byte[] bytes() { return bytes.clone(); }
    }
}
