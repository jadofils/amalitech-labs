package tests.validation;

import exceptions.SubjectValidationException;
import model.subject.CoreSubject;
import model.subject.Subject;
import utils.validators.SubjectValidator;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

class SubjectValidatorTest {

    @Test
    @DisplayName("A well-formed subject passes validation")
    void validSubjectPassesTest() {
        Subject subject = new CoreSubject("Mathematics", "MATH01");
        assertDoesNotThrow(() -> SubjectValidator.validateSubject(subject));
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "  "})
    @DisplayName("Empty or blank subject name fails")
    void emptyNameFailsTest(String name) {
        Subject subject = new CoreSubject(name, "MATH01");
        SubjectValidationException ex = assertThrows(SubjectValidationException.class,
                () -> SubjectValidator.validateSubject(subject));
        assertEquals("Subject name cannot be empty.", ex.getMessage());
    }

    @Test
    @DisplayName("Subject name shorter than 3 characters fails")
    void shortNameFailsTest() {
        Subject subject = new CoreSubject("Ma", "MATH01");
        SubjectValidationException ex = assertThrows(SubjectValidationException.class,
                () -> SubjectValidator.validateSubject(subject));
        assertEquals("Subject name must be between 3 and 100 characters.", ex.getMessage());
    }

    @Test
    @DisplayName("Subject name longer than 100 characters fails")
    void longNameFailsTest() {
        Subject subject = new CoreSubject("M".repeat(101), "MATH01");
        SubjectValidationException ex = assertThrows(SubjectValidationException.class,
                () -> SubjectValidator.validateSubject(subject));
        assertEquals("Subject name must be between 3 and 100 characters.", ex.getMessage());
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "math01", "MATH", "01MATH", "MATH1", "M1"})
    @DisplayName("Subject code not matching LETTERS+DIGITS (e.g. MATH01) fails")
    void invalidCodeFailsTest(String code) {
        Subject subject = new CoreSubject("Mathematics", code);
        SubjectValidationException ex = assertThrows(SubjectValidationException.class,
                () -> SubjectValidator.validateSubject(subject));
        assertNotNull(ex.getMessage());
    }

    @Test
    @DisplayName("Empty subject code gives the empty-specific message")
    void emptyCodeMessageTest() {
        Subject subject = new CoreSubject("Mathematics", "");
        SubjectValidationException ex = assertThrows(SubjectValidationException.class,
                () -> SubjectValidator.validateSubject(subject));
        assertEquals("Subject code cannot be empty.", ex.getMessage());
    }

    @Test
    @DisplayName("Malformed non-empty subject code gives the format-specific message")
    void malformedCodeMessageTest() {
        Subject subject = new CoreSubject("Mathematics", "math01");
        SubjectValidationException ex = assertThrows(SubjectValidationException.class,
                () -> SubjectValidator.validateSubject(subject));
        assertEquals("Subject code must follow format like MATH101.", ex.getMessage());
    }

    @ParameterizedTest
    @ValueSource(strings = {"MATH01", "ENGL01", "SCIE01", "MUSC01", "AB12", "ABCDEFGH99"})
    @DisplayName("Subject codes matching LETTERS(>=2)+DIGITS(>=2) pass")
    void validCodeFormatsPassTest(String code) {
        Subject subject = new CoreSubject("Mathematics", code);
        assertDoesNotThrow(() -> SubjectValidator.validateSubject(subject));
    }
}
