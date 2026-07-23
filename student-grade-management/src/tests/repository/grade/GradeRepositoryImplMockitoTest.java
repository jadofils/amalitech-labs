package tests.repository.grade;

import main.exceptions.GradeException;
import main.model.grade.Grade;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import main.repository.grade.GradeRepositoryImpl;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Uses mocked Grade objects to test storage/lookup logic in isolation from
 * the real Grade class's ID/date auto-generation, and to exhaust the fixed
 * 200-slot capacity cheaply.
 */
class GradeRepositoryImplMockitoTest {

    private Grade mockGradeWithId(String id) {
        Grade grade = mock(Grade.class);
        when(grade.getGradeId()).thenReturn(id);
        return grade;
    }

    @Test
    @DisplayName("findGradeById() matches purely on ID")
    void findMatchesOnIdOnlyTest() {
        GradeRepositoryImpl repository = new GradeRepositoryImpl();
        Grade mockGrade = mockGradeWithId("MOCK-GRD-1");
        repository.addGrade(mockGrade);

        assertSame(mockGrade, repository.findGradeById("MOCK-GRD-1"));
    }

    @Test
    @DisplayName("findGradesByStudentId() reads studentId, not gradeId, to filter")
    void findGradesByStudentIdReadsStudentIdTest() {
        GradeRepositoryImpl repository = new GradeRepositoryImpl();
        Grade mockGrade = mockGradeWithId("MOCK-GRD-2");
        when(mockGrade.getStudentId()).thenReturn("STU001");
        repository.addGrade(mockGrade);

        assertEquals(1, repository.findGradesByStudentId("STU001").size());
        assertTrue(repository.findGradesByStudentId("STU002").isEmpty());
        verify(mockGrade, atLeastOnce()).getStudentId();
    }

    @Test
    @DisplayName("Adding grades up to the fixed capacity (200) succeeds; the next one overflows")
    void capacityOverflowTest() {
        GradeRepositoryImpl repository = new GradeRepositoryImpl();

        for (int i = 0; i < 200; i++) {
            repository.addGrade(mockGradeWithId("MOCK-GRD-" + i));
        }
        assertEquals(200, repository.getGradeCount());

        RuntimeException ex = assertThrows(GradeException.class,
                () -> repository.addGrade(mockGradeWithId("ONE-TOO-MANY")));
        assertEquals("Cannot add more grades. Storage is full.", ex.getMessage());
    }
}
