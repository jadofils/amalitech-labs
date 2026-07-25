package tests.grade;

import main.exceptions.InvalidGradeException;
import main.model.enums.LetterGrade;
import main.model.enums.SubjectType;
import main.model.grade.Grade;
import main.model.subject.CoreSubject;
import main.model.subject.ElectiveSubject;
import main.model.subject.Subject;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.*;

class GradeTest {

    private final Subject math = new CoreSubject("Mathematics", "MATH01");

    @Test
    @DisplayName("Valid grade is recorded and gets an auto-generated ID and date")
    void validGradeConstructionTest() {
        Grade grade = new Grade("STU001", math, 85.0);

        assertEquals(85.0, grade.getGrade());
        assertEquals("STU001", grade.getStudentId());
        assertSame(math, grade.getSubject());
        assertTrue(grade.getGradeId().matches("^GRD\\d{3,}$"));
        assertTrue(grade.getDate().matches("^\\d{2}-\\d{2}-\\d{4}$"));
    }

    // NOTE: this codebase's Grade constructor throws InvalidGradeException
    // (not GradeException) on an out-of-range value - InvalidGradeException
    // carries the attempted value, which Main's recordGrade/bulkImportGrades
    // flows use to offer a retry.
    @Test
    @DisplayName("Grade above 100 is rejected")
    void gradeAboveMaxThrowsTest() {
        InvalidGradeException ex = assertThrows(InvalidGradeException.class, () -> new Grade("STU001", math, 100.1));
        assertTrue(ex.getMessage().startsWith("Grade must be between 0 and 100."));
        assertEquals(100.1, ex.getAttemptedGrade());
    }

    @Test
    @DisplayName("Negative grade is rejected")
    void negativeGradeThrowsTest() {
        InvalidGradeException ex = assertThrows(InvalidGradeException.class, () -> new Grade("STU001", math, -0.1));
        assertTrue(ex.getMessage().startsWith("Grade must be between 0 and 100."));
        assertEquals(-0.1, ex.getAttemptedGrade());
    }

    @Test
    @DisplayName("Each grade gets a distinct, format-correct ID")
    void gradeIdsAreUniqueAndFormattedTest() {
        // gradeCounter is static on Grade and shared with every other test in
        // this run - assert format and uniqueness, never an exact value.
        Grade first = new Grade("STU001", math, 50.0);
        Grade second = new Grade("STU001", math, 60.0);
        assertNotEquals(first.getGradeId(), second.getGradeId());
    }

    @ParameterizedTest
    @CsvSource({
            "100.0, A",
            "85.0, A",
            "84.9, B",
            "70.0, B",
            "69.9, C",
            "55.0, C",
            "54.9, D",
            "40.0, D",
            "39.9, F",
            "0.0, F"
    })
    @DisplayName("Letter grade boundaries match LetterGrade.fromNumeric")
    void letterGradeBoundariesTest(double numeric, String expectedLetter) {
        Grade grade = new Grade("STU001", math, numeric);
        assertEquals(LetterGrade.valueOf(expectedLetter), grade.getLetterGrade());
    }

    @Test
    @DisplayName("getSubjectType() delegates to the underlying subject")
    void getSubjectTypeDelegatesTest() {
        Subject elective = new ElectiveSubject("Music", "MUSC01");
        Grade grade = new Grade("STU001", elective, 70.0);
        assertEquals(SubjectType.ELECTIVE, grade.getSubjectType());
    }

    @Test
    @DisplayName("recordGrade() rejects an out-of-range value and leaves the stored grade unchanged")
    void recordGradeRejectsInvalidValueTest() {
        Grade grade = new Grade("STU001", math, 85.0);

        boolean accepted = grade.recordGrade(150.0);

        assertFalse(accepted);
        assertEquals(85.0, grade.getGrade());
    }

    @Test
    @DisplayName("validateGrade() reports range validity without mutating state")
    void validateGradeIsPureTest() {
        Grade grade = new Grade("STU001", math, 85.0);
        assertTrue(grade.validateGrade(0.0));
        assertTrue(grade.validateGrade(100.0));
        assertFalse(grade.validateGrade(-1.0));
        assertFalse(grade.validateGrade(100.1));
        assertEquals(85.0, grade.getGrade());
    }

    @Test
    @DisplayName("reconstruct() bypasses range validation and preserves the given ID/date exactly")
    void reconstructBypassesValidationTest() {
        // This is a documented quirk (see Lab 2's technical documentation):
        // reconstruct() is meant for rehydrating already-persisted grades, so
        // it does not re-run the 0-100 guard the public constructor enforces.
        Grade grade = Grade.reconstruct("GRD999", "STU001", math, 150.0, "01-01-2020");

        assertEquals("GRD999", grade.getGradeId());
        assertEquals("STU001", grade.getStudentId());
        assertEquals("01-01-2020", grade.getDate());
        assertEquals(150.0, grade.getGrade());
    }

    @Test
    @DisplayName("displayGradeDetails() does not throw")
    void displayGradeDetailsTest() {
        Grade grade = new Grade("STU001", math, 85.0);
        assertDoesNotThrow(grade::displayGradeDetails);
    }
}
