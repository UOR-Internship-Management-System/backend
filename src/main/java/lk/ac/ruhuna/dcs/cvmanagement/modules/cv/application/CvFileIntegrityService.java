package lk.ac.ruhuna.dcs.cvmanagement.modules.cv.application;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import lk.ac.ruhuna.dcs.cvmanagement.infrastructure.latex.LatexProperties;
import lk.ac.ruhuna.dcs.cvmanagement.infrastructure.storage.FileStorageException;
import lk.ac.ruhuna.dcs.cvmanagement.infrastructure.storage.FileStoragePort;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

/** Reads a stored generated PDF with bounded size and verifies its persisted SHA-256 metadata. */
@Service
public class CvFileIntegrityService {
    private final FileStoragePort cvFileStorage;
    private final long maxBytes;

    public CvFileIntegrityService(
            @Qualifier("cvFileStorage") FileStoragePort cvFileStorage,
            LatexProperties properties) {
        this.cvFileStorage = cvFileStorage;
        this.maxBytes = properties.maxOutputBytes();
    }

    public byte[] readVerified(String storageKey, long expectedSize, String expectedSha256) {
        if (storageKey == null || storageKey.isBlank() || expectedSize < 1 || expectedSize > maxBytes
                || expectedSha256 == null || !expectedSha256.matches("^[0-9a-f]{64}$")) {
            throw new FileIntegrityException();
        }
        try (InputStream input = cvFileStorage.open(storageKey)) {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            ByteArrayOutputStream output = new ByteArrayOutputStream((int) Math.min(expectedSize, 64 * 1024));
            byte[] buffer = new byte[16 * 1024];
            long total = 0;
            int read;
            while ((read = input.read(buffer)) != -1) {
                total += read;
                if (total > maxBytes || total > expectedSize) throw new FileIntegrityException();
                digest.update(buffer, 0, read);
                output.write(buffer, 0, read);
            }
            String actualSha256 = HexFormat.of().formatHex(digest.digest());
            if (total != expectedSize || !MessageDigest.isEqual(
                    actualSha256.getBytes(java.nio.charset.StandardCharsets.US_ASCII),
                    expectedSha256.getBytes(java.nio.charset.StandardCharsets.US_ASCII))) {
                throw new FileIntegrityException();
            }
            byte[] bytes = output.toByteArray();
            if (bytes.length < 5 || bytes[0] != '%' || bytes[1] != 'P' || bytes[2] != 'D' || bytes[3] != 'F' || bytes[4] != '-') {
                throw new FileIntegrityException();
            }
            return bytes;
        } catch (FileIntegrityException exception) {
            throw exception;
        } catch (IOException | FileStorageException | NoSuchAlgorithmException exception) {
            throw new FileIntegrityException();
        }
    }

    /** Internal marker; callers map it to the endpoint-specific safe public error. */
    public static final class FileIntegrityException extends RuntimeException {
        private FileIntegrityException() { super("Generated PDF integrity verification failed."); }
    }
}
