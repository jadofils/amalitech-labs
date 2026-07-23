package tests.enums;

import main.model.enums.LetterGrade;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.assertEquals;

// LetterGrade.fromNumeric is a pure static function with no collaborators to
// mock, so there is no Mockito counterpart for this one - see tests/README.md.
class LetterGradeTest {

    @ParameterizedTest
    @CsvSource({
            "100.0, A",
            "85.0, A",
            "84.999, B",
            "70.0, B",
            "69.999, C",
            "55.0, C",
            "54.999, D",
            "40.0, D",
            "39.999, F",
            "0.0, F"
    })
    @DisplayName("fromNumeric() boundaries are inclusive on the lower bound of each band")
    void fromNumericBoundariesTest(double numeric, String expectedLetter) {
        assertEquals(LetterGrade.valueOf(expectedLetter), LetterGrade.fromNumeric(numeric));
    }
}
