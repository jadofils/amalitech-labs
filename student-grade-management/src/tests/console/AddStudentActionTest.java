package tests.console;

import console.AddStudentAction;
import manager.GradeManager;
import manager.StudentManager;
import model.enums.Role;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import repository.student.StudentRepositoryImpl;
import repository.subject.SubjectRepositoryImpl;
import service.GradeService;
import service.GradeServiceImpl;
import service.StudentService;
import service.StudentServiceImpl;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Wires the real StudentServiceImpl/StudentRepositoryImpl/GradeManager stack
 * (the same wiring StudentManagerTest uses) to verify AddStudentAction
 * end-to-end: what it prints, and that the student it builds is actually
 * persisted afterward. AddStudentActionMockitoTest verifies the same
 * branches purely through interaction verification on a mocked StudentManager.
 */
class AddStudentActionTest {

    private StudentManager studentManager;

    @BeforeEach
    void setUp() {
        StudentRepositoryImpl studentRepository = new StudentRepositoryImpl();
        SubjectRepositoryImpl subjectRepository = new SubjectRepositoryImpl();
        GradeService gradeService = new GradeServiceImpl(studentRepository, subjectRepository);
        GradeManager gradeManager = new GradeManager(gradeService, subjectRepository);
        StudentService studentService = new StudentServiceImpl(studentRepository);
        studentManager = new StudentManager(studentService, gradeManager);
    }

    private String runAddStudent(String scriptedInput) {
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
    @DisplayName("Adding a Regular student reports a 50% passing grade, no honors line, and persists the student")
    void addsRegularStudentTest() {
        String output = runAddStudent("John Doe\n20\njohn.doe@school.edu\n1234567890\n1\n\n");

        assertTrue(output.contains("Student added successfully!"));
        assertTrue(output.contains("Passing Grade: 50%"));
        assertFalse(output.contains("Honors Eligible"));
        assertTrue(studentManager.getAllStudents().stream()
                .anyMatch(s -> s.getName().equals("John Doe")));
    }

    @Test
    @DisplayName("Adding an Honors student reports a 60% passing grade, an honors-eligible line, and persists the student")
    void addsHonorsStudentTest() {
        String output = runAddStudent("Jane Smith\n21\njane.smith@school.edu\n9876543210\n2\n\n");

        assertTrue(output.contains("Student added successfully!"));
        assertTrue(output.contains("Passing Grade: 60%"));
        assertTrue(output.contains("Honors Eligible: Yes"));
        assertTrue(studentManager.getAllStudents().stream()
                .anyMatch(s -> s.getName().equals("Jane Smith")));
    }

    @Test
    @DisplayName("A non-numeric age prints 'Invalid age.' and the student is never persisted")
    void invalidAgeDoesNotAddStudentTest() {
        int before = studentManager.getStudentCount();

        String output = runAddStudent("John Doe\nabc\njohn.doe@school.edu\n1234567890\n1\n");

        assertTrue(output.contains("Invalid age."));
        assertEquals(before, studentManager.getStudentCount());
    }

    @Test
    @DisplayName("An out-of-range type selection prints 'Invalid selection.' and the student is never persisted")
    void invalidTypeSelectionDoesNotAddStudentTest() {
        int before = studentManager.getStudentCount();

        String output = runAddStudent("John Doe\n20\njohn.doe@school.edu\n1234567890\n9\n");

        assertTrue(output.contains("Invalid selection."));
        assertEquals(before, studentManager.getStudentCount());
    }

    @Test
    @DisplayName("Menu metadata: option 1, label \"Add Student\", authorized only for TEACHER")
    void menuMetadataTest() {
        AddStudentAction action = new AddStudentAction(
                new Scanner(new ByteArrayInputStream(new byte[0])), studentManager);

        assertEquals(1, action.getOptionNumber());
        assertEquals("Add Student", action.getLabel());
        assertTrue(action.isAuthorizedFor(Role.TEACHER));
        assertFalse(action.isAuthorizedFor(Role.STUDENT));
    }
}
