package tests.service;

import main.exceptions.StudentValidationException;
import main.model.student.RegularStudent;
import main.model.student.Student;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import main.repository.student.StudentRepository;
import main.service.StudentServiceImpl;

import java.util.List;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Mocks StudentRepository to verify StudentServiceImpl's own logic - that it
 * validates before delegating, and never touches the main.repository when
 * validation fails - independent of StudentRepositoryImpl's real behaviour.
 */
class StudentServiceImplMockitoTest {

    @Test
    @DisplayName("addStudent() delegates to the main.repository exactly once for a valid student")
    void addValidStudentDelegatesTest() {
        StudentRepository repository = mock(StudentRepository.class);
        StudentServiceImpl service = new StudentServiceImpl(repository);
        Student student = new RegularStudent("Valid Student", 17, "valid@school.edu", "1234567890");

        service.addStudent(student);

        verify(repository, times(1)).addStudent(student);
    }

    @Test
    @DisplayName("addStudent() never reaches the main.repository for an invalid student")
    void addInvalidStudentNeverCallsRepositoryTest() {
        StudentRepository repository = mock(StudentRepository.class);
        StudentServiceImpl service = new StudentServiceImpl(repository);
        Student invalid = new RegularStudent("Bad Phone", 17, "valid@school.edu", "123");

        assertThrows(StudentValidationException.class, () -> service.addStudent(invalid));

        verify(repository, never()).addStudent(any());
    }

    @Test
    @DisplayName("getStudentById() delegates to the main.repository")
    void getStudentByIdDelegatesTest() {
        StudentRepository repository = mock(StudentRepository.class);
        Student student = mock(Student.class);
        when(repository.findStudentById("STU001")).thenReturn(student);
        StudentServiceImpl service = new StudentServiceImpl(repository);

        assertSame(student, service.getStudentById("STU001"));
        verify(repository, times(1)).findStudentById("STU001");
    }

    @Test
    @DisplayName("getAllStudents() delegates to the main.repository")
    void getAllStudentsDelegatesTest() {
        StudentRepository repository = mock(StudentRepository.class);
        List<Student> students = List.of(mock(Student.class), mock(Student.class));
        when(repository.getAllStudents()).thenReturn(students);
        StudentServiceImpl service = new StudentServiceImpl(repository);

        assertSame(students, service.getAllStudents());
    }

    @Test
    @DisplayName("updateStudent() never reaches the main.repository for an invalid update")
    void updateInvalidStudentNeverCallsRepositoryTest() {
        StudentRepository repository = mock(StudentRepository.class);
        StudentServiceImpl service = new StudentServiceImpl(repository);
        Student invalid = new RegularStudent("Bad", 17, "valid@school.edu", "1234567890"); // name too short

        assertThrows(StudentValidationException.class, () -> service.updateStudent(invalid));

        verify(repository, never()).updateStudent(any());
    }

    @Test
    @DisplayName("deleteStudent() delegates to the main.repository with the given ID")
    void deleteStudentDelegatesTest() {
        StudentRepository repository = mock(StudentRepository.class);
        StudentServiceImpl service = new StudentServiceImpl(repository);

        service.deleteStudent("STU001");

        verify(repository, times(1)).deleteStudent("STU001");
    }
}
