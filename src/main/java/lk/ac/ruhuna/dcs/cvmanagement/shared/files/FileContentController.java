package lk.ac.ruhuna.dcs.cvmanagement.shared.files;

import java.io.InputStream;
import java.util.UUID;
import lk.ac.ruhuna.dcs.cvmanagement.infrastructure.storage.FileAssetEntity;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.CacheControl;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Serves stored file bytes as an unguessable capability URL.
 *
 * <p>This endpoint is intentionally unauthenticated. The frontend renders profile photos through
 * {@code <img src>}, which cannot carry an {@code Authorization} header, so access is instead
 * gated on knowledge of a random version-4 UUID (122 bits of entropy). Anyone holding the URL can
 * read the file, so only student-supplied profile assets — never academic records, CVs or exports —
 * may be exposed here.
 */
@RestController
@RequestMapping("/api/v1/files")
public class FileContentController {

    private final ProfileFileService profileFileService;

    public FileContentController(ProfileFileService profileFileService) {
        this.profileFileService = profileFileService;
    }

    @GetMapping("/{fileAssetId}/content")
    public ResponseEntity<InputStreamResource> download(@PathVariable UUID fileAssetId) {
        FileAssetEntity asset = profileFileService.require(fileAssetId);
        InputStream content = profileFileService.open(asset);

        return ResponseEntity.ok()
            .contentType(MediaType.parseMediaType(asset.getMimeType()))
            .contentLength(asset.getFileSizeBytes())
            .eTag('"' + asset.getChecksumSha256() + '"')
            .cacheControl(CacheControl.noCache().cachePrivate())
            .header(
                HttpHeaders.CONTENT_DISPOSITION,
                ContentDisposition.inline()
                    .filename(asset.getFileName())
                    .build()
                    .toString())
            // Stored files are user-supplied; never let a browser sniff them into something
            // executable.
            .header("X-Content-Type-Options", "nosniff")
            .header("Content-Security-Policy", "default-src 'none'; sandbox")
            .body(new InputStreamResource(content));
    }
}
