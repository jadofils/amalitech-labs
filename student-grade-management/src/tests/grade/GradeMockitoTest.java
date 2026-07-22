package tests.grade;

import model.enums.SubjectType;
import model.grade.Grade;
import model.subject.Subject;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Mocks Subject (Grade's one real collaborator) so Grade's own logic can be
 * verified in isolation from CoreSubject/ElectiveSubject's implementation.
 */
class GradeMockitoTest {

    @Test
    @DisplayName("getSubjectType() calls through to subject.getSubjectType() exactly once")
    void getSubjectTypeDelegatesToMock() {
        Subject subject = mock(Subject.class);
        when(subject.getSubjectType()).thenReturn(SubjectType.CORE);
        Grade grade = new Grade("STU001", subject, 85.0);

        SubjectType type = grade.getSubjectType();

        assertEquals(SubjectType.CORE, type);
        verify(subject, times(1)).getSubjectType();
    }

    @Test
    @DisplayName("displayGradeDetails() prints the subject via subject.displaySubjectDetails()")
    void displayGradeDetailsDelegatesToSubject() {
        Subject subject = mock(Subject.class);
        when(subject.getSubjectType()).thenReturn(SubjectType.CORE);
        Grade grade = new Grade("STU001", subject, 85.0);

        grade.displayGradeDetails();

        verify(subject, times(1)).displaySubjectDetails();
    }

    @Test
    @DisplayName("Constructing a Grade does not itself query the subject's name or code")
    void constructionDoesNotTouchSubjectDetails() {
        Subject subject = mock(Subject.class);

        new Grade("STU001", subject, 85.0);

        verify(subject, never()).getSubjectName();
        verify(subject, never()).getSubjectCode();
        verify(subject, never()).getSubjectType();
    }
}
