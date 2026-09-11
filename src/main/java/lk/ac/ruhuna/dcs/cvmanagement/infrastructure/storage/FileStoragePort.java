package lk.ac.ruhuna.dcs.cvmanagement.infrastructure.storage;

import java.io.InputStream;

/**
 * Application-facing port for durable file storage.
 *
 * <p>Storage keys are server-generated opaque relative paths. Client-supplied filenames must never be
 * used as storage keys.
 */
public interface FileStoragePort {

    StoredFile store(String storageKey, InputStream content);

    InputStream open(String storageKey);

    void delete(String storageKey);

    record StoredFile(long sizeBytes, String checksumSha256) {
    }
}
