package tests.enums;

import main.model.enums.GpaLetterGrade;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.*;

// Pure enum, no collaborators - no GpaLetterGradeMockitoTest (see tests/README.md).
class GpaLetterGradeTest {

    @ParameterizedTest
    @CsvSource({
            "100, A", "93, A",
            "92, A_MINUS", "90, A_MINUS",
            "89, B_PLUS", "87, B_PLUS",
            "86, B", "83, B",
            "82, B_MINUS", "80, B_MINUS",
            "79, C_PLUS", "77, C_PLUS",
            "76, C", "73, C",
            "72, C_MINUS", "70, C_MINUS",
            "69, D_PLUS", "67, D_PLUS",
            "66, D", "60, D",
            "59, F", "0, F"
    })
    @DisplayName("fromPercentage() matches the ReadMe-v2.md grading table")
    void fromPercentageMatchesTableTest(double percentage, String expectedName) {
        assertEquals(GpaLetterGrade.valueOf(expectedName), GpaLetterGrade.fromPercentage(percentage));
    }

    // Regression guard for CHANGELOG.md KI-1: fromGpaPoints() must return the
    // constant *for* a GPA value, not the one above it, for every documented
    // GPA value - not just the boundaries that happened to already work.
    @ParameterizedTest
    @CsvSource({
            "4.0, A", "3.7, A_MINUS", "3.3, B_PLUS", "3.0, B", "2.7, B_MINUS",
            "2.3, C_PLUS", "2.0, C", "1.7, C_MINUS", "1.3, D_PLUS", "1.0, D", "0.0, F"
    })
    @DisplayName("fromGpaPoints() matches the ReadMe-v2.md grading table for every documented GPA value")
    void fromGpaPointsMatchesTableTest(double gpaPoints, String expectedName) {
        assertEquals(GpaLetterGrade.valueOf(expectedName), GpaLetterGrade.fromGpaPoints(gpaPoints));
    }

    @Test
    @DisplayName("Each constant's label matches its own name style (e.g. A_MINUS -> \"A-\")")
    void labelsMatchDocumentedLettersTest() {
        assertEquals("A", GpaLetterGrade.A.getLabel());
        assertEquals("A-", GpaLetterGrade.A_MINUS.getLabel());
        assertEquals("B+", GpaLetterGrade.B_PLUS.getLabel());
        assertEquals("F", GpaLetterGrade.F.getLabel());
    }
}
