package lk.ac.ruhuna.dcs.cvmanagement.shared.files;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
import lk.ac.ruhuna.dcs.cvmanagement.infrastructure.storage.FileAssetEntity;
import lk.ac.ruhuna.dcs.cvmanagement.infrastructure.storage.FileAssetRepository;
import lk.ac.ruhuna.dcs.cvmanagement.infrastructure.storage.FileStoragePort;
import lk.ac.ruhuna.dcs.cvmanagement.shared.api.dto.FileAssetResponse;
import lk.ac.ruhuna.dcs.cvmanagement.shared.error.BadRequestException;
import lk.ac.ruhuna.dcs.cvmanagement.shared.error.NotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

/**
 * Stores and resolves student-supplied file assets.
 *
 * <p>Storage keys are always server-generated. Client filenames are recorded as metadata only and
 * never influence the path on disk.
 */
@Service
public class ProfileFileService {

    private static final Logger LOGGER = LoggerFactory.getLogger(ProfileFileService.class);

    private final FileStoragePort fileStorage;
    private final FileAssetRepository fileAssetRepository;
    private final ProfileFileProperties properties;

    public ProfileFileService(
        FileStoragePort fileStorage,
        FileAssetRepository fileAssetRepository,
        ProfileFileProperties properties) {
        this.fileStorage = fileStorage;
        this.fileAssetRepository = fileAssetRepository;
        this.properties = properties;
    }

    /**
     * Validates and stores an uploaded file, returning the persisted asset metadata.
     *
     * @param keyPrefix server-controlled storage namespace, e.g. {@code profile-photo}
     */
    @Transactional
    public FileAssetEntity store(
        MultipartFile file,
        ProfileFileProperties.Constraint constraint,
        String keyPrefix,
        UUID ownerAccountId) {

        if (file == null || file.isEmpty()) {
            throw new UnprocessableUploadException("The multipart field \"file\" is required.");
        }
        if (!constraint.permitsMimeType(file.getContentType())) {
            throw new UnsupportedUploadTypeException(
                "The selected file type is not supported.", constraint.allowedMimeTypes());
        }
        String originalName = sanitizeFileName(file.getOriginalFilename());
        if (!constraint.permitsExtension(originalName)) {
            throw new UnsupportedUploadTypeException(
                "The selected file extension is not supported.", constraint.allowedExtensions());
        }
        if (file.getSize() > constraint.maxSizeBytes()) {
            throw new UploadTooLargeException(
                "The selected file is too large.", constraint.maxSizeBytes());
        }

        UUID assetId = UUID.randomUUID();
        String storageKey = "%s/%s".formatted(keyPrefix, assetId);

        FileStoragePort.StoredFile stored;
        try (InputStream content = file.getInputStream()) {
            stored = fileStorage.store(storageKey, content);
        } catch (IOException exception) {
            throw new BadRequestException("The uploaded file could not be read.");
        }

        FileAssetEntity asset = new FileAssetEntity();
        asset.setOwnerAccountId(ownerAccountId);
        asset.setFileName(originalName);
        asset.setStorageKey(storageKey);
        asset.setMimeType(file.getContentType().toLowerCase(Locale.ROOT).trim());
        asset.setFileSizeBytes(stored.sizeBytes());
        asset.setChecksumSha256(stored.checksumSha256());
        return fileAssetRepository.save(asset);
    }

    /** Removes both the metadata row and the stored bytes. Never throws for a missing asset. */
    @Transactional
    public void delete(UUID fileAssetId) {
        if (fileAssetId == null) {
            return;
        }
        fileAssetRepository.findById(fileAssetId).ifPresent(asset -> {
            fileAssetRepository.delete(asset);
            try {
                fileStorage.delete(asset.getStorageKey());
            } catch (RuntimeException exception) {
                // The metadata row is authoritative. A leftover blob is reclaimable; a dangling
                // reference is not, so never fail the transaction on a storage cleanup error.
                LOGGER.warn("Orphaned stored file remains at key={}", asset.getStorageKey(), exception);
            }
        });
    }

    @Transactional(readOnly = true)
    public FileAssetEntity require(UUID fileAssetId) {
        return fileAssetRepository.findById(fileAssetId)
            .orElseThrow(() -> new NotFoundException("File asset was not found."));
    }

    public InputStream open(FileAssetEntity asset) {
        return fileStorage.open(asset.getStorageKey());
    }

    /** Resolves a single asset id to its API representation. Returns {@code null} when unset. */
    @Transactional(readOnly = true)
    public FileAssetResponse resolve(UUID fileAssetId) {
        if (fileAssetId == null) {
            return null;
        }
        return fileAssetRepository.findById(fileAssetId).map(this::toResponse).orElse(null);
    }

    /**
     * Batch-resolves asset ids. Use this for collection endpoints — resolving per row would issue
     * one query per item.
     */
    @Transactional(readOnly = true)
    public Map<UUID, FileAssetResponse> resolveAll(Collection<UUID> fileAssetIds) {
        List<UUID> present = fileAssetIds.stream().filter(java.util.Objects::nonNull).distinct().toList();
        if (present.isEmpty()) {
            return Map.of();
        }
        return fileAssetRepository.findAllById(present).stream()
            .collect(Collectors.toMap(FileAssetEntity::getId, this::toResponse));
    }

    public FileAssetResponse toResponse(FileAssetEntity asset) {
        return new FileAssetResponse(
            asset.getId(),
            asset.getFileName(),
            asset.getMimeType(),
            asset.getFileSizeBytes(),
            "%s/api/v1/files/%s/content".formatted(properties.publicBaseUrl(), asset.getId()),
            asset.getCreatedAt());
    }

    public ProfileFileProperties properties() {
        return properties;
    }

    /** Strips any path component a client may have supplied. */
    private String sanitizeFileName(String originalFilename) {
        if (originalFilename == null || originalFilename.isBlank()) {
            return "upload";
        }
        String name = originalFilename.replace('\\', '/');
        int slash = name.lastIndexOf('/');
        String bare = slash >= 0 ? name.substring(slash + 1) : name;
        bare = bare.replaceAll("[\\p{Cntrl}]", "").trim();
        if (bare.isBlank()) {
            return "upload";
        }
        return bare.length() > 255 ? bare.substring(bare.length() - 255) : bare;
    }

    /** Maps to HTTP 422. */
    public static class UnprocessableUploadException extends RuntimeException {
        public UnprocessableUploadException(String message) {
            super(message);
        }
    }

    /** Maps to HTTP 415. */
    public static class UnsupportedUploadTypeException extends RuntimeException {
        private final transient List<String> permitted;

        public UnsupportedUploadTypeException(String message, List<String> permitted) {
            super(message);
            this.permitted = List.copyOf(permitted);
        }

        public List<String> permitted() {
            return permitted;
        }
    }

    /** Maps to HTTP 413. */
    public static class UploadTooLargeException extends RuntimeException {
        private final long maxSizeBytes;

        public UploadTooLargeException(String message, long maxSizeBytes) {
            super(message);
            this.maxSizeBytes = maxSizeBytes;
        }

        public long maxSizeBytes() {
            return maxSizeBytes;
        }
    }

    /** Convenience accessor used by the mapper layer. */
    public Function<UUID, FileAssetResponse> resolver() {
        return this::resolve;
    }
}
