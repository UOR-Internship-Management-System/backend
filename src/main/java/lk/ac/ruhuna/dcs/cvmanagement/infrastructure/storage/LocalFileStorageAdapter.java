package lk.ac.ruhuna.dcs.cvmanagement.infrastructure.storage;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

/**
 * Local-filesystem implementation of {@link FileStoragePort}.
 *
 * <p>Writes are performed through a temporary file followed by an atomic move when the underlying
 * filesystem supports it. This prevents partially written source files from appearing at their final
 * storage key.
 */
public class LocalFileStorageAdapter implements FileStoragePort {

    private static final int BUFFER_SIZE = 16 * 1024;

    private final Path root;

    public LocalFileStorageAdapter(StorageProperties properties) {
        this(properties.root());
    }

    public LocalFileStorageAdapter(Path root) {
        if (root == null) {
            throw new IllegalArgumentException("storage root must not be null");
        }
        this.root = root.toAbsolutePath().normalize();
        initializeRoot();
    }

    @Override
    public StoredFile store(String storageKey, InputStream content) {
        if (content == null) {
            throw new IllegalArgumentException("content must not be null");
        }

        Path target = resolveStorageKey(storageKey);
        Path parent = target.getParent();
        Path temporary = null;
        try {
            Files.createDirectories(parent);
            temporary = Files.createTempFile(parent, ".upload-", ".tmp");

            MessageDigest digest = sha256();
            try (DigestInputStream digestInput = new DigestInputStream(new BufferedInputStream(content), digest);
                    BufferedOutputStream output = new BufferedOutputStream(Files.newOutputStream(temporary))) {
                digestInput.transferTo(output);
            }

            long sizeBytes = Files.size(temporary);
            moveIntoPlace(temporary, target);
            temporary = null;
            return new StoredFile(sizeBytes, HexFormat.of().formatHex(digest.digest()));
        } catch (IOException exception) {
            throw new FileStorageException("Unable to store file content.", exception);
        } finally {
            deleteQuietly(temporary);
        }
    }

    @Override
    public InputStream open(String storageKey) {
        Path target = resolveStorageKey(storageKey);
        try {
            return new BufferedInputStream(Files.newInputStream(target));
        } catch (IOException exception) {
            throw new FileStorageException("Unable to open stored file.", exception);
        }
    }

    @Override
    public void delete(String storageKey) {
        Path target = resolveStorageKey(storageKey);
        try {
            Files.deleteIfExists(target);
        } catch (IOException exception) {
            throw new FileStorageException("Unable to delete stored file.", exception);
        }
    }

    private void initializeRoot() {
        try {
            Files.createDirectories(root);
            if (!Files.isDirectory(root) || !Files.isWritable(root)) {
                throw new FileStorageException("Configured storage root is not a writable directory.", null);
            }
        } catch (IOException exception) {
            throw new FileStorageException("Unable to initialize configured storage root.", exception);
        }
    }

    private Path resolveStorageKey(String storageKey) {
        if (storageKey == null || storageKey.isBlank()) {
            throw new IllegalArgumentException("storageKey must not be blank");
        }
        Path relative = Path.of(storageKey);
        if (relative.isAbsolute()) {
            throw new IllegalArgumentException("storageKey must be relative");
        }
        Path resolved = root.resolve(relative).normalize();
        if (!resolved.startsWith(root) || resolved.equals(root)) {
            throw new IllegalArgumentException("storageKey escapes the configured storage root");
        }
        return resolved;
    }

    private void moveIntoPlace(Path source, Path target) throws IOException {
        if (Files.exists(target)) {
            throw new IOException("A file already exists at the generated storage key.");
        }
        try {
            Files.move(source, target, StandardCopyOption.ATOMIC_MOVE);
        } catch (AtomicMoveNotSupportedException exception) {
            Files.move(source, target);
        }
    }

    private MessageDigest sha256() {
        try {
            return MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 is not available in this JVM.", exception);
        }
    }

    private void deleteQuietly(Path path) {
        if (path == null) {
            return;
        }
        try {
            Files.deleteIfExists(path);
        } catch (IOException ignored) {
            // The original storage failure is more useful to the caller than a cleanup failure.
        }
    }
}
