package tests.manager;

import exceptions.StudentNotFoundException;
import manager.GradeManager;
import manager.StudentManager;
import model.grade.Grade;
import model.student.RegularStudent;
import model.student.Student;
import model.subject.CoreSubject;
import model.subject.Subject;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import service.StudentService;

import java.util.List;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Mocks StudentService and GradeManager to verify StudentManager's hydration
 * logic in isolation - in particular, that it only ever hydrates a Student
 * that was actually returned, and never queries grades for one it couldn't
 * find.
 */
class StudentManagerMockitoTest {

    private final Subject subject = new CoreSubject("Mathematics", "MATH01");

    @Test
    @DisplayName("findStudent() hydrates the student's grade list from GradeManager")
    void findStudentHydratesGradesTest() {
        StudentService studentService = mock(StudentService.class);
        GradeManager gradeManager = mock(GradeManager.class);
        StudentManager manager = new StudentManager(studentService, gradeManager);
        // Explicit-ID constructor - deliberately not the auto-ID one, since
        // that ID is shared/static across the whole test run and must never
        // be assumed to equal a hardcoded literal like "STU001" (see
        // src/tests/README.md).
        Student student = new RegularStudent("STU001", "Musa Nkusi", 17, "musa@school.edu",
                "1234567890", model.enums.StudentStatus.ACTIVE);
        when(studentService.getStudentById("STU001")).thenReturn(student);
        when(gradeManager.getGradesForStudent("STU001")).thenReturn(
                List.of(new Grade("STU001", subject, 80.0), new Grade("STU001", subject, 90.0)));

        Student hydrated = manager.findStudent("STU001");

        assertEquals(List.of(80.0, 90.0), hydrated.getGrades());
        verify(gradeManager, times(1)).getGradesForStudent("STU001");
    }

    @Test
    @DisplayName("findStudent() never queries grades when the student cannot be found")
    void findStudentMissingNeverQueriesGradesTest() {
        StudentService studentService = mock(StudentService.class);
        GradeManager gradeManager = mock(GradeManager.class);
        StudentManager manager = new StudentManager(studentService, gradeManager);
        when(studentService.getStudentById("NOPE")).thenThrow(new StudentNotFoundException("not found"));

        Student result = manager.findStudent("NOPE");

        assertNull(result);
        verify(gradeManager, never()).getGradesForStudent(any());
    }

    @Test
    @DisplayName("getAllStudents() hydrates each returned student exactly once")
    void getAllStudentsHydratesEachOnceTest() {
        StudentService studentService = mock(StudentService.class);
        GradeManager gradeManager = mock(GradeManager.class);
        StudentManager manager = new StudentManager(studentService, gradeManager);
        Student first = new RegularStudent("STU001", "First Student", 17, "first@school.edu",
                "1234567890", model.enums.StudentStatus.ACTIVE);
        Student second = new RegularStudent("STU002", "Second Student", 17, "second@school.edu",
                "1234567890", model.enums.StudentStatus.ACTIVE);
        when(studentService.getAllStudents()).thenReturn(List.of(first, second));
        when(gradeManager.getGradesForStudent(anyString())).thenReturn(List.of());

        manager.getAllStudents();

        verify(gradeManager, times(1)).getGradesForStudent("STU001");
        verify(gradeManager, times(1)).getGradesForStudent("STU002");
    }

    @Test
    @DisplayName("addStudent() delegates to StudentService exactly once")
    void addStudentDelegatesTest() {
        StudentService studentService = mock(StudentService.class);
        GradeManager gradeManager = mock(GradeManager.class);
        StudentManager manager = new StudentManager(studentService, gradeManager);
        Student student = new RegularStudent("Musa Nkusi", 17, "musa@school.edu", "1234567890");

        manager.addStudent(student);

        verify(studentService, times(1)).addStudent(student);
    }

    @Test
    @DisplayName("updateStudent() delegates to StudentService with the exact same object")
    void updateStudentDelegatesTest() {
        StudentService studentService = mock(StudentService.class);
        GradeManager gradeManager = mock(GradeManager.class);
        StudentManager manager = new StudentManager(studentService, gradeManager);
        Student student = new RegularStudent("STU001", "Musa Nkusi", 17, "musa@school.edu",
                "1234567890", model.enums.StudentStatus.ACTIVE);

        manager.updateStudent(student);

        verify(studentService, times(1)).updateStudent(student);
    }

    @Test
    @DisplayName("deleteStudent() delegates to StudentService with the exact same ID")
    void deleteStudentDelegatesTest() {
        StudentService studentService = mock(StudentService.class);
        GradeManager gradeManager = mock(GradeManager.class);
        StudentManager manager = new StudentManager(studentService, gradeManager);

        manager.deleteStudent("STU001");

        verify(studentService, times(1)).deleteStudent("STU001");
    }

    @Test
    @DisplayName("viewAllStudents() prints a specific message and never hydrates when there are no students")
    void viewAllStudentsEmptyTest() {
        StudentService studentService = mock(StudentService.class);
        GradeManager gradeManager = mock(GradeManager.class);
        StudentManager manager = new StudentManager(studentService, gradeManager);
        when(studentService.getAllStudents()).thenReturn(List.of());

        String output = captureStdOut(manager::viewAllStudents);

        assertTrue(output.contains("No students found."));
        verify(gradeManager, never()).getGradesForStudent(any());
    }

    private String captureStdOut(Runnable action) {
        java.io.PrintStream original = System.out;
        java.io.ByteArrayOutputStream buffer = new java.io.ByteArrayOutputStream();
        System.setOut(new java.io.PrintStream(buffer));
        try {
            action.run();
        } finally {
            System.setOut(original);
        }
        return buffer.toString();
    }
}
