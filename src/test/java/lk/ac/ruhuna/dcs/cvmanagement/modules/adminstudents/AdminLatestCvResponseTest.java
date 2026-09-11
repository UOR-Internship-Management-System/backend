package lk.ac.ruhuna.dcs.cvmanagement.modules.adminstudents;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.OffsetDateTime;
import java.util.UUID;
import lk.ac.ruhuna.dcs.cvmanagement.modules.adminstudents.api.dto.response.AdminLatestCvResponse;
import lk.ac.ruhuna.dcs.cvmanagement.modules.adminstudents.domain.model.CvFreshnessStatus;
import lk.ac.ruhuna.dcs.cvmanagement.modules.adminstudents.domain.model.LatestCvAvailability;
import org.junit.jupiter.api.Test;

class AdminLatestCvResponseTest {

    @Test
    void notSavedFactoryProducesStrictEmptyMetadataState() {
        AdminLatestCvResponse response = AdminLatestCvResponse.notSaved();

        assertThat(response.availability()).isEqualTo(LatestCvAvailability.NOT_SAVED);
        assertThat(response.cvId()).isNull();
        assertThat(response.downloadUrl()).isNull();
    }

    @Test
    void availableFactoryBuildsCanonicalDownloadRoute() {
        UUID studentId = UUID.fromString("40000000-0000-4000-8000-000000000001");
        AdminLatestCvResponse response = AdminLatestCvResponse.available(
                UUID.fromString("550e8400-e29b-41d4-a716-446655440000"),
                3,
                OffsetDateTime.parse("2026-07-19T08:30:00Z"),
                OffsetDateTime.parse("2026-07-19T08:31:00Z"),
                CvFreshnessStatus.CURRENT,
                "SC_2022_12865_CV.pdf",
                184320,
                studentId);

        assertThat(response.downloadUrl())
                .isEqualTo("/admin/students/40000000-0000-4000-8000-000000000001/latest-cv/download");
    }

    @Test
    void rejectsUnsafeAvailableFilename() {
        assertThatThrownBy(() -> AdminLatestCvResponse.available(
                UUID.randomUUID(),
                1,
                OffsetDateTime.now(),
                OffsetDateTime.now(),
                CvFreshnessStatus.CURRENT,
                "../cv.pdf",
                100,
                UUID.randomUUID()))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
