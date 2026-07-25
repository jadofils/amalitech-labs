package tests.validation;

import main.exceptions.SubjectValidationException;
import main.model.subject.Subject;
import main.utils.validators.SubjectValidator;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Mirrors StudentValidatorMockitoTest's approach: mock the argument (Subject)
 * rather than the validator itself, to isolate each rule and verify ordering.
 */
class SubjectValidatorMockitoTest {

    private Subject validMock() {
        Subject subject = mock(Subject.class);
        when(subject.getSubjectName()).thenReturn("Mathematics");
        when(subject.getSubjectCode()).thenReturn("MATH01");
        return subject;
    }

    @Test
    @DisplayName("A fully-valid mock passes and reads both fields exactly once")
    void allFieldsValidChecksEveryRuleOnce() {
        Subject subject = validMock();

        assertDoesNotThrow(() -> SubjectValidator.validateSubject(subject));

        verify(subject, times(1)).getSubjectName();
        verify(subject, times(1)).getSubjectCode();
    }

    @Test
    @DisplayName("An invalid name short-circuits before the code is read")
    void invalidNameShortCircuitsTest() {
        Subject subject = validMock();
        when(subject.getSubjectName()).thenReturn("");

        assertThrows(SubjectValidationException.class, () -> SubjectValidator.validateSubject(subject));

        verify(subject, times(1)).getSubjectName();
        verify(subject, never()).getSubjectCode();
    }

    @Test
    @DisplayName("Name is validated before code")
    void nameCheckedBeforeCodeTest() {
        Subject subject = validMock();

        SubjectValidator.validateSubject(subject);

        var inOrder = inOrder(subject);
        inOrder.verify(subject).getSubjectName();
        inOrder.verify(subject).getSubjectCode();
    }
}
