package lk.ac.ruhuna.dcs.cvmanagement.infrastructure.storage;

import java.io.Serial;

/** Raised when the configured durable storage cannot complete a requested operation safely. */
public class FileStorageException extends RuntimeException {

    @Serial
    private static final long serialVersionUID = 1L;

    public FileStorageException(String message, Throwable cause) {
        super(message, cause);
    }
}
