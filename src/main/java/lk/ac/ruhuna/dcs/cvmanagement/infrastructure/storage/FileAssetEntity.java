package lk.ac.ruhuna.dcs.cvmanagement.infrastructure.storage;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.OffsetDateTime;
import java.util.UUID;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

/** Metadata row for a durable file stored outside PostgreSQL. */
@Entity
@Table(name = "file_asset", schema = "system")
public class FileAssetEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "file_asset_id", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "owner_account_id")
    private UUID ownerAccountId;

    @Column(name = "file_name", nullable = false, length = 255, updatable = false)
    private String fileName;

    @Column(name = "storage_key", nullable = false, unique = true, updatable = false, columnDefinition = "text")
    private String storageKey;

    @Column(name = "mime_type", nullable = false, length = 120, updatable = false)
    private String mimeType;

    @Column(name = "file_size_bytes", nullable = false, updatable = false)
    private long fileSizeBytes;

    @Column(name = "checksum_sha256", nullable = false, length = 64, updatable = false, columnDefinition = "char(64)")
    @JdbcTypeCode(SqlTypes.CHAR)
    private String checksumSha256;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    public FileAssetEntity() {
    }

    public UUID getId() {
        return id;
    }

    public UUID getOwnerAccountId() {
        return ownerAccountId;
    }

    public void setOwnerAccountId(UUID ownerAccountId) {
        this.ownerAccountId = ownerAccountId;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getStorageKey() {
        return storageKey;
    }

    public void setStorageKey(String storageKey) {
        this.storageKey = storageKey;
    }

    public String getMimeType() {
        return mimeType;
    }

    public void setMimeType(String mimeType) {
        this.mimeType = mimeType;
    }

    public long getFileSizeBytes() {
        return fileSizeBytes;
    }

    public void setFileSizeBytes(long fileSizeBytes) {
        this.fileSizeBytes = fileSizeBytes;
    }

    public String getChecksumSha256() {
        return checksumSha256;
    }

    public void setChecksumSha256(String checksumSha256) {
        this.checksumSha256 = checksumSha256;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }
}
