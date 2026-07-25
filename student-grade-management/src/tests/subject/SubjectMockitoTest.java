package tests.subject;

import main.model.subject.CoreSubject;
import main.model.subject.ElectiveSubject;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.mockito.Mockito.*;

/**
 * Subject has no external collaborators to mock, so these tests use a spy on
 * the real object to verify displaySubjectDetails() actually reads its own
 * name/code via the public getters, rather than some duplicated internal state.
 */
class SubjectMockitoTest {

    @Test
    @DisplayName("CoreSubject.displaySubjectDetails() reads name and code through their getters")
    void coreDisplayUsesGetters() {
        CoreSubject spy = spy(new CoreSubject("Mathematics", "MATH01"));

        spy.displaySubjectDetails();

        verify(spy, atLeastOnce()).getSubjectName();
        verify(spy, atLeastOnce()).getSubjectCode();
    }

    @Test
    @DisplayName("ElectiveSubject.displaySubjectDetails() reads name and code through their getters")
    void electiveDisplayUsesGetters() {
        ElectiveSubject spy = spy(new ElectiveSubject("Music", "MUSC01"));

        spy.displaySubjectDetails();

        verify(spy, atLeastOnce()).getSubjectName();
        verify(spy, atLeastOnce()).getSubjectCode();
    }
}
