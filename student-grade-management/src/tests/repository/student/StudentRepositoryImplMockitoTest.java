package tests.repository.student;

import exceptions.StudentException;
import exceptions.StudentNotFoundException;
import model.student.Student;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import repository.student.StudentRepositoryImpl;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Uses mocked Student objects so the repository's storage/lookup logic can be
 * tested in isolation from any concrete Student subclass, and so the fixed
 * capacity (50) can be exhausted without needing 50 validator-passing real
 * students (the repository never calls StudentValidator - only the service
 * layer does).
 */
class StudentRepositoryImplMockitoTest {

    private Student mockStudentWithId(String id) {
        Student student = mock(Student.class);
        when(student.getStudentId()).thenReturn(id);
        return student;
    }

    @Test
    @DisplayName("findStudentById() matches purely on ID, independent of the Student implementation")
    void findMatchesOnIdOnlyTest() {
        StudentRepositoryImpl repository = new StudentRepositoryImpl();
        Student mockStudent = mockStudentWithId("MOCK001");
        repository.addStudent(mockStudent);

        Student found = repository.findStudentById("MOCK001");

        assertSame(mockStudent, found);
        verify(mockStudent, atLeastOnce()).getStudentId();
    }

    @Test
    @DisplayName("Adding students up to the fixed capacity (50) succeeds; the next one overflows")
    void capacityOverflowTest() {
        StudentRepositoryImpl repository = new StudentRepositoryImpl();
        int alreadySeeded = repository.getAllStudents().size(); // 5

        // Fill the remaining slots in the 50-slot backing array.
        for (int i = 0; i < 50 - alreadySeeded; i++) {
            repository.addStudent(mockStudentWithId("MOCK" + i));
        }
        assertEquals(50, repository.getAllStudents().size());

        Student oneTooMany = mockStudentWithId("ONE-TOO-MANY");
        StudentException ex = assertThrows(StudentException.class,
                () -> repository.addStudent(oneTooMany));
        assertEquals("Cannot add more students. Storage is full.", ex.getMessage());
    }

    @Test
    @DisplayName("deleteStudent() only needs getStudentId() to identify a match")
    void deleteOnlyReadsStudentId() {
        StudentRepositoryImpl repository = new StudentRepositoryImpl();
        Student mockStudent = mockStudentWithId("MOCK002");
        repository.addStudent(mockStudent);

        repository.deleteStudent("MOCK002");

        assertThrows(StudentNotFoundException.class, () -> repository.findStudentById("MOCK002"));
        verify(mockStudent, never()).getName();
        verify(mockStudent, never()).getEmail();
    }
}
