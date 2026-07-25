package tests.console;

import main.console.ViewStudentsAction;
import main.manager.StudentManager;
import main.model.student.HonorsStudent;
import main.model.student.RegularStudent;
import main.model.student.Student;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Scanner;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Mocks StudentManager to verify ViewStudentsAction's main.console output and its
 * interaction with the collaborator in isolation - in particular, that
 * getAllStudents() is queried exactly once per execute() and that the
 * division-by-zero guard around the class average is exercised for an empty
 * roster. ViewStudentsActionTest verifies the same class through a real
 * StudentManager/StudentServiceImpl/GradeManager stack.
 */
class ViewStudentsActionMockitoTest {

    @Test
    @DisplayName("execute() queries getAllStudents() on the StudentManager exactly once")
    void executeQueriesAllStudentsExactlyOnceTest() {
        StudentManager studentManager = mock(StudentManager.class);
        when(studentManager.getAllStudents()).thenReturn(List.of());

        runAction(studentManager);

        verify(studentManager, times(1)).getAllStudents();
    }

    @Test
    @DisplayName("An empty roster prints 'Total Students: 0' but never 'Average Class Grade' (division-by-zero guard)")
    void emptyRosterOmitsAverageClassGradeTest() {
        StudentManager studentManager = mock(StudentManager.class);
        when(studentManager.getAllStudents()).thenReturn(List.of());

        String output = runAction(studentManager);

        assertTrue(output.contains("Total Students: 0"));
        assertFalse(output.contains("Average Class Grade"));
    }

    @Test
    @DisplayName("A passing Regular student and an eligible Honors student are both reflected correctly")
    void mixedRosterReflectsPerStudentStatusTest() {
        Student regular = new RegularStudent("Regular One", 16, "r1@school.edu", "1234567890");
        regular.addGrade(80.0);
        Student honors = new HonorsStudent("Honors One", 17, "h1@school.edu", "1234567890");
        honors.addGrade(90.0); // >= 60% honors threshold
        StudentManager studentManager = mock(StudentManager.class);
        when(studentManager.getAllStudents()).thenReturn(List.of(regular, honors));

        String output = runAction(studentManager);

        assertTrue(output.contains("Total Students: 2"));
        assertTrue(output.contains("Average Class Grade: 85.0%")); // (80 + 90) / 2

        String regularLine = lineContaining(output, regular.getName());
        assertTrue(regularLine.contains("Passing"));

        String honorsDetail = lineAfter(output, honors.getName());
        assertTrue(honorsDetail.contains("Enrolled Subjects: 1"));
        assertTrue(honorsDetail.contains("Honors Eligible"));
    }

    @Test
    @DisplayName("An Honors student below the honors threshold has no 'Honors Eligible' in their detail row")
    void ineligibleHonorsStudentOmitsHonorsEligibleTest() {
        Student honors = new HonorsStudent("Honors Two", 17, "h2@school.edu", "1234567890"); // no grades, avg 0.0
        StudentManager studentManager = mock(StudentManager.class);
        when(studentManager.getAllStudents()).thenReturn(List.of(honors));

        String output = runAction(studentManager);

        String honorsDetail = lineAfter(output, honors.getName());
        assertTrue(honorsDetail.contains("Enrolled Subjects: 0"));
        assertFalse(honorsDetail.contains("Honors Eligible"));
    }

    private Scanner scriptedScanner() {
        return new Scanner(new ByteArrayInputStream("\n".getBytes(StandardCharsets.UTF_8)));
    }

    private String runAction(StudentManager studentManager) {
        PrintStream originalOut = System.out;
        ByteArrayOutputStream captured = new ByteArrayOutputStream();
        try (Scanner scanner = scriptedScanner()) {
            ViewStudentsAction action = new ViewStudentsAction(scanner, studentManager);
            System.setOut(new PrintStream(captured, true, StandardCharsets.UTF_8));
            action.execute();
        } finally {
            System.setOut(originalOut);
        }
        return captured.toString(StandardCharsets.UTF_8);
    }

    private String lineContaining(String text, String needle) {
        for (String line : text.split("\n")) {
            if (line.contains(needle)) {
                return line;
            }
        }
        throw new AssertionError("No line containing: " + needle);
    }

    private String lineAfter(String text, String needle) {
        String[] lines = text.split("\n");
        for (int i = 0; i < lines.length - 1; i++) {
            if (lines[i].contains(needle)) {
                return lines[i + 1];
            }
        }
        throw new AssertionError("No line found after: " + needle);
    }
}
