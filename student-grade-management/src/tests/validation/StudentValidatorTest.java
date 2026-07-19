package tests.validation;

import exceptions.StudentValidationException;
import model.enums.StudentStatus;
import model.student.RegularStudent;
import model.student.Student;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import validation.StudentValidator;

import static org.junit.jupiter.api.Assertions.*;

class StudentValidatorTest {

    @Test
    @DisplayName("A well-formed student passes validation")
    void validStudentPassesTest() {
        Student student = new RegularStudent("James Musoni", 17, "james.musoni@amalitech.com", "1234567890");
        assertDoesNotThrow(() -> StudentValidator.validateStudent(student));
    }

    @Test
    @DisplayName("Null student is rejected")
    void nullStudentFailsTest() {
        StudentValidationException ex = assertThrows(StudentValidationException.class,
                () -> StudentValidator.validateStudent(null));
        assertEquals("Student object cannot be null.", ex.getMessage());
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "   "})
    @DisplayName("Empty or blank name fails with the exact expected message")
    void emptyOrBlankNameFailsTest(String name) {
        Student student = new RegularStudent(name, 17, "james@amalitech.com", "1234567890");
        StudentValidationException ex = assertThrows(StudentValidationException.class,
                () -> StudentValidator.validateStudent(student));
        assertEquals("Student name cannot be empty or contain only spaces.", ex.getMessage());
    }

    @Test
    @DisplayName("Name shorter than 4 characters fails")
    void shortNameFailsTest() {
        Student student = new RegularStudent("Ab", 17, "james@amalitech.com", "1234567890");
        StudentValidationException ex = assertThrows(StudentValidationException.class,
                () -> StudentValidator.validateStudent(student));
        assertEquals("Name must be between 4 and 100 characters.", ex.getMessage());
    }

    @Test
    @DisplayName("Name longer than 100 characters fails")
    void longNameFailsTest() {
        String name = "A".repeat(101);
        Student student = new RegularStudent(name, 17, "james@amalitech.com", "1234567890");
        StudentValidationException ex = assertThrows(StudentValidationException.class,
                () -> StudentValidator.validateStudent(student));
        assertEquals("Name must be between 4 and 100 characters.", ex.getMessage());
    }

    @ParameterizedTest
    @ValueSource(strings = {"James123", "James-Musoni", "James_Musoni", "James1"})
    @DisplayName("Name containing digits/symbols fails")
    void invalidCharactersInNameFailTest(String name) {
        Student student = new RegularStudent(name, 17, "james@amalitech.com", "1234567890");
        StudentValidationException ex = assertThrows(StudentValidationException.class,
                () -> StudentValidator.validateStudent(student));
        assertEquals("Name must contain only letters and spaces.", ex.getMessage());
    }

    @ParameterizedTest
    @ValueSource(ints = {-1, 0, 4, 101, 200})
    @DisplayName("Age outside 5-100 fails")
    void invalidAgeFailsTest(int age) {
        Student student = new RegularStudent("Musa Nkusi", age, "musa@amalitech.com", "1234567890");
        StudentValidationException ex = assertThrows(StudentValidationException.class,
                () -> StudentValidator.validateStudent(student));
        assertEquals("Age must be between 5 and 100.", ex.getMessage());
    }

    @ParameterizedTest
    @ValueSource(ints = {5, 17, 100})
    @DisplayName("Age within 5-100 (inclusive) passes")
    void validAgeBoundariesPassTest(int age) {
        Student student = new RegularStudent("Musa Nkusi", age, "musa@amalitech.com", "1234567890");
        assertDoesNotThrow(() -> StudentValidator.validateStudent(student));
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "musa@gmail", "invalid-email", "musa@amalitech", "@amalitech.com", "musa amalitech.com"})
    @DisplayName("Malformed email fails")
    void invalidEmailFailsTest(String email) {
        Student student = new RegularStudent("Musa Nkusi", 17, email, "1234567890");
        StudentValidationException ex = assertThrows(StudentValidationException.class,
                () -> StudentValidator.validateStudent(student));
        assertEquals("Invalid email format.", ex.getMessage());
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "123", "123456789", "12345678901", "abcd123456", "123-456-7890"})
    @DisplayName("Phone that is not exactly 10 digits fails")
    void invalidPhoneFailsTest(String phone) {
        Student student = new RegularStudent("Musa Nkusi", 17, "musa@amalitech.com", phone);
        StudentValidationException ex = assertThrows(StudentValidationException.class,
                () -> StudentValidator.validateStudent(student));
        assertEquals("Phone number must be exactly 10 digits.", ex.getMessage());
    }

    @Test
    @DisplayName("Null status fails")
    void nullStatusFailsTest() {
        Student student = new RegularStudent("Musa Nkusi", 17, "musa@amalitech.com", "1234567890");
        student.setStatus(null);
        StudentValidationException ex = assertThrows(StudentValidationException.class,
                () -> StudentValidator.validateStudent(student));
        assertEquals("Student status cannot be null.", ex.getMessage());
    }

    @ParameterizedTest
    @CsvSource({"ACTIVE", "INACTIVE", "SUSPENDED", "GRADUATED"})
    @DisplayName("Every non-null status value is accepted")
    void everyNonNullStatusPassesTest(String status) {
        Student student = new RegularStudent("Musa Nkusi", 17, "musa@amalitech.com", "1234567890");
        student.setStatus(StudentStatus.valueOf(status));
        assertDoesNotThrow(() -> StudentValidator.validateStudent(student));
    }

    @Test
    @DisplayName("An ID shorter than 4 characters fails validateId() directly")
    void shortIdFailsTest() {
        StudentValidationException ex = assertThrows(StudentValidationException.class,
                () -> StudentValidator.validateId("ab"));
        assertEquals("Student ID must be at least 4 characters and alphanumeric.", ex.getMessage());
    }

    @Test
    @DisplayName("Validation checks the ID before the name, short-circuiting on the first failure")
    void idIsValidatedBeforeNameTest() {
        Student student = new RegularStudent("Musa Nkusi", 17, "musa@amalitech.com", "1234567890");
        student.setStudentId("ab"); // valid name, invalid ID
        StudentValidationException ex = assertThrows(StudentValidationException.class,
                () -> StudentValidator.validateStudent(student));
        assertEquals("Student ID must be at least 4 characters and alphanumeric.", ex.getMessage());
    }
}
