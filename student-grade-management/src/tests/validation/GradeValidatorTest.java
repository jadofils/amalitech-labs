package tests.validation;

import main.exceptions.InvalidGradeException;
import main.utils.validators.GradeValidator;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class GradeValidatorTest {

    @Test
    @DisplayName("A well-formed grade ID, student ID, and value pass without throwing")
    void validForImportPassesTest() {
        assertDoesNotThrow(() -> GradeValidator.validateForImport("GRD001", "STU001", 85.0));
    }

    @Test
    @DisplayName("A malformed grade ID is rejected")
    void malformedGradeIdFailsTest() {
        InvalidGradeException ex = assertThrows(InvalidGradeException.class,
                () -> GradeValidator.validateForImport("BADID", "STU001", 85.0));
        assertTrue(ex.getMessage().contains("Grade ID"));
    }

    @Test
    @DisplayName("A malformed student ID is rejected")
    void malformedStudentIdFailsTest() {
        InvalidGradeException ex = assertThrows(InvalidGradeException.class,
                () -> GradeValidator.validateForImport("GRD001", "BADID", 85.0));
        assertTrue(ex.getMessage().contains("Student ID"));
    }

    @Test
    @DisplayName("A grade value outside 0-100 is rejected")
    void outOfRangeGradeFailsTest() {
        InvalidGradeException ex = assertThrows(InvalidGradeException.class,
                () -> GradeValidator.validateForImport("GRD001", "STU001", 150.0));
        assertEquals(150.0, ex.getAttemptedGrade());
    }

    @Test
    @DisplayName("A negative grade value is rejected")
    void negativeGradeFailsTest() {
        InvalidGradeException ex = assertThrows(InvalidGradeException.class,
                () -> GradeValidator.validateForImport("GRD001", "STU001", -5.0));
        assertEquals(-5.0, ex.getAttemptedGrade());
    }

    @Test
    @DisplayName("A null grade ID is rejected, not thrown as an NPE")
    void nullGradeIdFailsTest() {
        assertThrows(InvalidGradeException.class,
                () -> GradeValidator.validateForImport(null, "STU001", 85.0));
    }

    @Test
    @DisplayName("A null student ID is rejected, not thrown as an NPE")
    void nullStudentIdFailsTest() {
        assertThrows(InvalidGradeException.class,
                () -> GradeValidator.validateForImport("GRD001", null, 85.0));
    }
}
