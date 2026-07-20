package tests.validation;

import exceptions.StudentValidationException;
import model.enums.StudentStatus;
import model.student.Student;
import utils.validators.StudentValidator;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * StudentValidator has no collaborators of its own, so instead of mocking the
 * class under test, these tests mock its *argument* (Student). That gives two
 * things the real-object tests in StudentValidatorTest cannot cheaply give:
 *  1. True per-rule isolation - every field except the one under test is
 *     stubbed to an unconditionally-valid value, with no dependency on which
 *     Student subclass or constructor is used.
 *  2. Interaction verification - proof that validateStudent() actually
 *     short-circuits on the first failing rule instead of reading every field.
 */
class StudentValidatorMockitoTest {

    private Student validMock() {
        Student student = mock(Student.class);
        when(student.getStudentId()).thenReturn("STU001");
        when(student.getName()).thenReturn("Musa Nkusi");
        when(student.getAge()).thenReturn(17);
        when(student.getEmail()).thenReturn("musa@amalitech.com");
        when(student.getPhone()).thenReturn("1234567890");
        when(student.getStatus()).thenReturn(StudentStatus.ACTIVE);
        when(student.calculateAverageGrade()).thenReturn(75.0);
        return student;
    }

    @Test
    @DisplayName("A fully-valid mock passes, and every rule is checked exactly once")
    void allFieldsValidChecksEveryRuleOnce() {
        Student student = validMock();

        assertDoesNotThrow(() -> StudentValidator.validateStudent(student));

        verify(student, times(1)).getStudentId();
        verify(student, times(1)).getName();
        verify(student, times(1)).getAge();
        verify(student, times(1)).getEmail();
        verify(student, times(1)).getPhone();
        verify(student, times(1)).getStatus();
        verify(student, times(1)).calculateAverageGrade();
    }

    @Test
    @DisplayName("An invalid ID short-circuits before any other field is read")
    void invalidIdShortCircuitsTest() {
        Student student = validMock();
        when(student.getStudentId()).thenReturn("ab");

        assertThrows(StudentValidationException.class, () -> StudentValidator.validateStudent(student));

        verify(student, never()).getName();
        verify(student, never()).getAge();
        verify(student, never()).getEmail();
        verify(student, never()).getPhone();
        verify(student, never()).getStatus();
        verify(student, never()).calculateAverageGrade();
    }

    @Test
    @DisplayName("An invalid name short-circuits before age/email/phone/status are read")
    void invalidNameShortCircuitsTest() {
        Student student = validMock();
        when(student.getName()).thenReturn("");

        assertThrows(StudentValidationException.class, () -> StudentValidator.validateStudent(student));

        verify(student, times(1)).getStudentId();
        verify(student, times(1)).getName();
        verify(student, never()).getAge();
        verify(student, never()).getEmail();
        verify(student, never()).getPhone();
        verify(student, never()).getStatus();
    }

    @Test
    @DisplayName("An invalid age short-circuits before email/phone/status are read")
    void invalidAgeShortCircuitsTest() {
        Student student = validMock();
        when(student.getAge()).thenReturn(200);

        assertThrows(StudentValidationException.class, () -> StudentValidator.validateStudent(student));

        verify(student, never()).getEmail();
        verify(student, never()).getPhone();
        verify(student, never()).getStatus();
    }

    @Test
    @DisplayName("An invalid phone short-circuits before status/grades are read")
    void invalidPhoneShortCircuitsTest() {
        Student student = validMock();
        when(student.getPhone()).thenReturn("123");

        assertThrows(StudentValidationException.class, () -> StudentValidator.validateStudent(student));

        verify(student, never()).getStatus();
        verify(student, never()).calculateAverageGrade();
    }

    @Test
    @DisplayName("Fields are read in the order: id, name, age, email, phone, status, grades")
    void fieldsAreReadInDocumentedOrderTest() {
        Student student = validMock();

        StudentValidator.validateStudent(student);

        var inOrder = inOrder(student);
        inOrder.verify(student).getStudentId();
        inOrder.verify(student).getName();
        inOrder.verify(student).getAge();
        inOrder.verify(student).getEmail();
        inOrder.verify(student).getPhone();
        inOrder.verify(student).getStatus();
        inOrder.verify(student).calculateAverageGrade();
    }
}
