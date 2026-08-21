package lk.ac.ruhuna.dcs.cvmanagement.modules.cv;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;
import java.security.MessageDigest;
import java.time.Duration;
import java.util.HexFormat;
import lk.ac.ruhuna.dcs.cvmanagement.infrastructure.latex.LatexProperties;
import lk.ac.ruhuna.dcs.cvmanagement.infrastructure.storage.FileStoragePort;
import lk.ac.ruhuna.dcs.cvmanagement.modules.cv.application.CvFileIntegrityService;
import org.junit.jupiter.api.Test;

class CvFileIntegrityServiceTest {

    @Test
    void returnsOnlyPdfWhoseSizeAndShaMatchPersistedMetadata() throws Exception {
        FileStoragePort storage = mock(FileStoragePort.class);
        byte[] pdf = "%PDF-1.7\nverified".getBytes(java.nio.charset.StandardCharsets.US_ASCII);
        String sha = HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(pdf));
        when(storage.open("cv/objects/object.pdf")).thenReturn(new ByteArrayInputStream(pdf));
        CvFileIntegrityService service = new CvFileIntegrityService(storage, properties());

        assertThat(service.readVerified("cv/objects/object.pdf", pdf.length, sha)).isEqualTo(pdf);
    }

    @Test
    void checksumMismatchAndNonPdfContentFailClosed() throws Exception {
        FileStoragePort storage = mock(FileStoragePort.class);
        byte[] pdf = "%PDF-1.7\nverified".getBytes(java.nio.charset.StandardCharsets.US_ASCII);
        when(storage.open("cv/objects/object.pdf")).thenReturn(new ByteArrayInputStream(pdf));
        CvFileIntegrityService service = new CvFileIntegrityService(storage, properties());

        assertThatThrownBy(() -> service.readVerified("cv/objects/object.pdf", pdf.length, "0".repeat(64)))
                .isInstanceOf(CvFileIntegrityService.FileIntegrityException.class);

        byte[] text = "plain text".getBytes(java.nio.charset.StandardCharsets.UTF_8);
        String sha = HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(text));
        when(storage.open("cv/objects/text.pdf")).thenReturn(new ByteArrayInputStream(text));
        assertThatThrownBy(() -> service.readVerified("cv/objects/text.pdf", text.length, sha))
                .isInstanceOf(CvFileIntegrityService.FileIntegrityException.class);
    }

    private LatexProperties properties() {
        return new LatexProperties("xelatex", Duration.ofSeconds(5), 1024 * 1024, 1);
    }
}
