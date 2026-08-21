package lk.ac.ruhuna.dcs.cvmanagement.modules.cv;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
import lk.ac.ruhuna.dcs.cvmanagement.modules.cv.application.CvSourceFingerprintService;
import lk.ac.ruhuna.dcs.cvmanagement.modules.cv.domain.model.CvConfiguration;
import lk.ac.ruhuna.dcs.cvmanagement.modules.cv.domain.model.CvDocumentModel;
import org.junit.jupiter.api.Test;

class CvSourceFingerprintServiceTest {

    private final CvSourceFingerprintService service = new CvSourceFingerprintService();

    @Test
    void isDeterministicForEquivalentCanonicalSnapshots() {
        CvDocumentModel first = model("Summary");
        CvDocumentModel second = model("Summary");

        assertThat(service.fingerprint(first))
                .isEqualTo(service.fingerprint(second))
                .matches("[0-9a-f]{64}");
    }

    @Test
    void changesWhenRenderedSourceContentChanges() {
        assertThat(service.fingerprint(model("First summary")))
                .isNotEqualTo(service.fingerprint(model("Changed summary")));
    }

    private CvDocumentModel model(String summary) {
        UUID studentId = UUID.fromString("10000000-0000-0000-0000-000000000001");
        OffsetDateTime updatedAt = OffsetDateTime.parse("2026-08-19T02:00:00Z");
        return new CvDocumentModel(
                new CvDocumentModel.Identity(studentId, "Student One", "student@ruh.ac.lk", updatedAt),
                new CvDocumentModel.Profile(
                        UUID.fromString("20000000-0000-0000-0000-000000000001"),
                        "Student One",
                        "student@example.com",
                        "Software Engineering Student",
                        summary,
                        "+94 77 000 0000",
                        "Matara",
                        3L,
                        updatedAt),
                List.of(),
                List.of(),
                List.of(),
                List.of(),
                List.of(),
                List.of(),
                List.of(),
                null,
                new CvConfiguration(List.of(), List.of(), List.of(), List.of(), List.of()));
    }
}
