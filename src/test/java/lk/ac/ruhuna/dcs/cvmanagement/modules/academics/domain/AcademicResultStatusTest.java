package lk.ac.ruhuna.dcs.cvmanagement.modules.academics.domain;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class AcademicResultStatusTest {

    @Test
    void acceptsOnlyTheApprovedOfficialVocabulary() {
        assertThat(AcademicResultStatus.fromExternalValue(" passed ")).contains(AcademicResultStatus.PASSED);
        assertThat(AcademicResultStatus.fromExternalValue("FAILED")).contains(AcademicResultStatus.FAILED);
        assertThat(AcademicResultStatus.fromExternalValue("absent")).contains(AcademicResultStatus.ABSENT);
        assertThat(AcademicResultStatus.fromExternalValue("INCOMPLETE")).isEmpty();
        assertThat(AcademicResultStatus.fromExternalValue("WITHHELD")).isEmpty();
    }

    @Test
    void derivesStatusFromTheApprovedGradeRules() {
        assertThat(AcademicResultStatus.expectedFor("C", true)).isEqualTo(AcademicResultStatus.PASSED);
        assertThat(AcademicResultStatus.expectedFor("C-", false)).isEqualTo(AcademicResultStatus.FAILED);
        assertThat(AcademicResultStatus.expectedFor("E", false)).isEqualTo(AcademicResultStatus.FAILED);
        assertThat(AcademicResultStatus.expectedFor("E*", false)).isEqualTo(AcademicResultStatus.ABSENT);
    }
}
