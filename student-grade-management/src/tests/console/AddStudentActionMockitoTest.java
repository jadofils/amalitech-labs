package tests.console;

import main.console.AddStudentAction;
import main.manager.StudentManager;
import main.model.student.HonorsStudent;
import main.model.student.RegularStudent;
import main.model.student.Student;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Mocks StudentManager to verify AddStudentAction's branching in isolation:
 * which concrete Student subtype it builds for each type selection, and that
 * it never calls addStudent() at all when validation of the scripted input
 * fails before a Student is even built.
 */
class AddStudentActionMockitoTest {

    private String runAddStudent(StudentManager studentManager, String scriptedInput) {
        Scanner scanner = new Scanner(new ByteArrayInputStream(scriptedInput.getBytes(StandardCharsets.UTF_8)));
        PrintStream originalOut = System.out;
        ByteArrayOutputStream captured = new ByteArrayOutputStream();
        try {
            System.setOut(new PrintStream(captured, true, StandardCharsets.UTF_8));
            new AddStudentAction(scanner, studentManager).execute();
        } finally {
            System.setOut(originalOut);
        }
        return captured.toString(StandardCharsets.UTF_8);
    }

    @Test
    @DisplayName("Type selection \"1\" builds and adds a RegularStudent exactly once")
    void regularSelectionAddsRegularStudentTest() {
        StudentManager studentManager = mock(StudentManager.class);

        String output = runAddStudent(studentManager, "John Doe\n20\njohn.doe@school.edu\n1234567890\n1\n\n");

        ArgumentCaptor<Student> captor = ArgumentCaptor.forClass(Student.class);
        verify(studentManager, times(1)).addStudent(captor.capture());
        Student added = captor.getValue();
        assertTrue(added instanceof RegularStudent);
        assertEquals("John Doe", added.getName());
        assertTrue(output.contains("Student added successfully!"));
        assertTrue(output.contains("Passing Grade: 50%"));
        assertFalse(output.contains("Honors Eligible"));
    }

    @Test
    @DisplayName("Type selection \"2\" builds and adds an HonorsStudent exactly once")
    void honorsSelectionAddsHonorsStudentTest() {
        StudentManager studentManager = mock(StudentManager.class);

        String output = runAddStudent(studentManager, "Jane Smith\n21\njane.smith@school.edu\n9876543210\n2\n\n");

        ArgumentCaptor<Student> captor = ArgumentCaptor.forClass(Student.class);
        verify(studentManager, times(1)).addStudent(captor.capture());
        Student added = captor.getValue();
        assertTrue(added instanceof HonorsStudent);
        assertEquals("Jane Smith", added.getName());
        assertTrue(output.contains("Student added successfully!"));
        assertTrue(output.contains("Passing Grade: 60%"));
        assertTrue(output.contains("Honors Eligible: Yes"));
    }

    @Test
    @DisplayName("A non-numeric age prints 'Invalid age.' and never calls addStudent()")
    void invalidAgeNeverAddsStudentTest() {
        StudentManager studentManager = mock(StudentManager.class);

        String output = runAddStudent(studentManager, "John Doe\nabc\njohn.doe@school.edu\n1234567890\n1\n");

        assertTrue(output.contains("Invalid age."));
        verify(studentManager, never()).addStudent(any());
    }

    @Test
    @DisplayName("An out-of-range type selection prints 'Invalid selection.' and never calls addStudent()")
    void invalidTypeSelectionNeverAddsStudentTest() {
        StudentManager studentManager = mock(StudentManager.class);

        String output = runAddStudent(studentManager, "John Doe\n20\njohn.doe@school.edu\n1234567890\n9\n");

        assertTrue(output.contains("Invalid selection."));
        verify(studentManager, never()).addStudent(any());
    }
}
