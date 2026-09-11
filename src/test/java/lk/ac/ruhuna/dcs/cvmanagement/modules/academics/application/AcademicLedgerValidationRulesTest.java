package lk.ac.ruhuna.dcs.cvmanagement.modules.academics.application;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import org.junit.jupiter.api.Test;

class AcademicLedgerValidationRulesTest {

    @Test
    void extractsCohortYearOnlyFromCanonicalStudentIndex() {
        assertThat(AcademicLedgerValidationRules.cohortYear("SC/2025/00001")).isEqualTo((short) 2025);
        assertThat(AcademicLedgerValidationRules.cohortYear("SC-2025-001")).isNull();
    }

    @Test
    void semesterComparisonTreatsRomanAndArabicFormsAsEquivalent() {
        assertThat(AcademicLedgerValidationRules.sameSemester("Semester 1", "Semester I")).isTrue();
        assertThat(AcademicLedgerValidationRules.sameSemester("Semester 2", "Semester II")).isTrue();
        assertThat(AcademicLedgerValidationRules.sameSemester("Semester 1", "Semester II")).isFalse();
    }

    @Test
    void creditComparisonUsesExactDecimalValueWithoutFloatingPointConversion() {
        assertThat(AcademicLedgerValidationRules.sameCredits(
                        new BigDecimal("1.5"), new BigDecimal("1.50")))
                .isTrue();
        assertThat(AcademicLedgerValidationRules.sameCredits(
                        new BigDecimal("2.0"), new BigDecimal("3.0")))
                .isFalse();
    }

    @Test
    void rejectedValuesAreControlSanitizedAndLengthBounded() {
        String value = "bad\u0000value" + "x".repeat(200);
        String sanitized = AcademicLedgerValidationRules.sanitizeRejectedValue(value);
        assertThat(sanitized).doesNotContain("\u0000").hasSize(120);
    }
}
