package lk.ac.ruhuna.dcs.cvmanagement.modules.academics;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import lk.ac.ruhuna.dcs.cvmanagement.modules.academics.api.error.AcademicLedgerApiException;
import lk.ac.ruhuna.dcs.cvmanagement.modules.academics.domain.AcademicLedgerUploadSort;
import org.junit.jupiter.api.Test;

class AcademicLedgerUploadSortTest {

    @Test
    void defaultsToUploadedAtDescending() {
        assertThat(AcademicLedgerUploadSort.fromApiValue(null))
                .isEqualTo(AcademicLedgerUploadSort.UPLOADED_AT_DESC);
    }

    @Test
    void acceptsEveryFrozenOpenApiSort() {
        assertThat(AcademicLedgerUploadSort.fromApiValue("uploadedAt,asc"))
                .isEqualTo(AcademicLedgerUploadSort.UPLOADED_AT_ASC);
        assertThat(AcademicLedgerUploadSort.fromApiValue("originalFilename,asc"))
                .isEqualTo(AcademicLedgerUploadSort.ORIGINAL_FILENAME_ASC);
        assertThat(AcademicLedgerUploadSort.fromApiValue("status,asc"))
                .isEqualTo(AcademicLedgerUploadSort.STATUS_ASC);
        assertThat(AcademicLedgerUploadSort.fromApiValue("status,desc"))
                .isEqualTo(AcademicLedgerUploadSort.STATUS_DESC);
    }

    @Test
    void rejectsBlankSortWhenParameterIsSupplied() {
        assertThatThrownBy(() -> AcademicLedgerUploadSort.fromApiValue("   "))
                .isInstanceOf(AcademicLedgerApiException.class);
    }

    @Test
    void rejectsUnapprovedSortProperties() {
        assertThatThrownBy(() -> AcademicLedgerUploadSort.fromApiValue("fileHash,asc"))
                .isInstanceOf(AcademicLedgerApiException.class);
    }
}
