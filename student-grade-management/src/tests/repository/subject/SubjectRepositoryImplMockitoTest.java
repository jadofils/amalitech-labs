package tests.repository.subject;

import exceptions.SubjectException;
import exceptions.SubjectNotFoundException;
import model.subject.Subject;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import repository.subject.SubjectRepositoryImpl;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Unlike StudentRepositoryImpl, this repository calls SubjectValidator on
 * every addSubject(), so mocks used here must stub name/code with values that
 * satisfy SubjectValidator (see SubjectRepositoryImplTest's addSubjectValidatesTest
 * for the case where they don't).
 */
class SubjectRepositoryImplMockitoTest {

    private Subject validMockSubject(String code) {
        Subject subject = mock(Subject.class);
        when(subject.getSubjectName()).thenReturn("Test Subject");
        when(subject.getSubjectCode()).thenReturn(code);
        return subject;
    }

    @Test
    @DisplayName("findSubjectByCode() matches purely on code")
    void findMatchesOnCodeOnlyTest() {
        SubjectRepositoryImpl repository = new SubjectRepositoryImpl();
        Subject mockSubject = validMockSubject("ZZZZ99");
        repository.addSubject(mockSubject);

        Subject found = repository.findSubjectByCode("ZZZZ99");

        assertSame(mockSubject, found);
    }

    @Test
    @DisplayName("Adding subjects up to the fixed capacity (50) succeeds; the next one overflows")
    void capacityOverflowTest() {
        SubjectRepositoryImpl repository = new SubjectRepositoryImpl();
        int alreadySeeded = repository.getAllSubjects().size(); // 6

        for (int i = 0; i < 50 - alreadySeeded; i++) {
            repository.addSubject(validMockSubject("AB" + (10 + i)));
        }
        assertEquals(50, repository.getAllSubjects().size());

        SubjectException ex = assertThrows(SubjectException.class,
                () -> repository.addSubject(validMockSubject("ZZ99")));
        assertEquals("Cannot add more subjects. Storage is full.", ex.getMessage());
    }
}
