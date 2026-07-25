package tests.manager;

import main.manager.StudentManager;
import main.manager.StudentSearcher;
import main.model.enums.StudentType;
import main.model.student.HonorsStudent;
import main.model.student.RegularStudent;
import main.model.student.Student;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Mocks StudentManager to verify StudentSearcher's own filtering logic in
 * isolation from its real implementation.
 */
class StudentSearcherMockitoTest {

    @Test
    @DisplayName("searchById() delegates to StudentManager.findStudent() exactly once")
    void searchByIdDelegatesTest() {
        StudentManager studentManager = mock(StudentManager.class);
        StudentSearcher searcher = new StudentSearcher(studentManager);
        Student student = new RegularStudent("Musa Nkusi", 17, "musa@school.edu", "1234567890");
        when(studentManager.findStudent("STU001")).thenReturn(student);

        List<Student> results = searcher.searchById("STU001");

        assertEquals(List.of(student), results);
        verify(studentManager, times(1)).findStudent("STU001");
    }

    @Test
    @DisplayName("searchByName() reads getAllStudents() from StudentManager, not some other source")
    void searchByNameDelegatesTest() {
        StudentManager studentManager = mock(StudentManager.class);
        StudentSearcher searcher = new StudentSearcher(studentManager);
        Student alice = new RegularStudent("Alice Johnson", 17, "alice@school.edu", "1234567890");
        Student bob = new RegularStudent("Bob Smith", 17, "bob@school.edu", "1234567890");
        when(studentManager.getAllStudents()).thenReturn(List.of(alice, bob));

        List<Student> results = searcher.searchByName("alice");

        assertEquals(1, results.size());
        assertEquals("Alice Johnson", results.get(0).getName());
        verify(studentManager, times(1)).getAllStudents();
    }

    @Test
    @DisplayName("searchByType() correctly separates a mocked mix of Regular and Honors students")
    void searchByTypeDelegatesTest() {
        StudentManager studentManager = mock(StudentManager.class);
        StudentSearcher searcher = new StudentSearcher(studentManager);
        Student regular = new RegularStudent("Regular Student", 17, "r@school.edu", "1234567890");
        Student honors = new HonorsStudent("Honors Student", 17, "h@school.edu", "1234567890");
        when(studentManager.getAllStudents()).thenReturn(List.of(regular, honors));

        assertEquals(1, searcher.searchByType(StudentType.HONORS).size());
        assertEquals(1, searcher.searchByType(StudentType.REGULAR).size());
    }
}
