package tests.console;

import main.console.RecordGradeAction;
import main.exceptions.StudentNotFoundException;
import main.manager.GradeManager;
import main.manager.StudentManager;
import main.model.enums.Role;
import main.model.grade.Grade;
import main.model.student.Student;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import main.repository.student.StudentRepositoryImpl;
import main.repository.subject.SubjectRepositoryImpl;
import main.service.GradeService;
import main.service.StudentService;
import main.service.GradeServiceImpl;
import main.service.StudentServiceImpl;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Scanner;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Wires the real StudentManager/GradeManager stack (the same wiring Main.java
 * uses) to verify RecordGradeAction's happy paths and validation branches
 * end-to-end. RecordGradeActionMockitoTest verifies the same behavior purely
 * through mocked collaborators, including the branch (no subjects available
 * for a type) that the real seeded main.repository can never actually produce.
 */
class RecordGradeActionTest {

    private StudentRepositoryImpl studentRepository;
    private SubjectRepositoryImpl subjectRepository;
    private GradeManager gradeManager;
    private StudentManager studentManager;

    @BeforeEach
    void setUp() {
        studentRepository = new StudentRepositoryImpl();
        subjectRepository = new SubjectRepositoryImpl();
        GradeService gradeService = new GradeServiceImpl(studentRepository, subjectRepository);
        gradeManager = new GradeManager(gradeService, subjectRepository);
        StudentService studentService = new StudentServiceImpl(studentRepository);
        studentManager = new StudentManager(studentService, gradeManager);
    }

    private String runWithInput(String scriptedInput) {
        PrintStream originalOut = System.out;
        ByteArrayOutputStream captured = new ByteArrayOutputStream();
        try (Scanner scanner = new Scanner(new ByteArrayInputStream(scriptedInput.getBytes(StandardCharsets.UTF_8)))) {
            System.setOut(new PrintStream(captured, true, StandardCharsets.UTF_8));
            new RecordGradeAction(scanner, studentManager, gradeManager).execute();
        } finally {
            System.setOut(originalOut);
        }
        return captured.toString(StandardCharsets.UTF_8);
    }

    @Test
    @DisplayName("execute() throws StudentNotFoundException when the student ID isn't found")
    void studentNotFoundThrowsTest() {
        Scanner scanner = new Scanner(new ByteArrayInputStream("NOPE\n".getBytes(StandardCharsets.UTF_8)));
        RecordGradeAction action = new RecordGradeAction(scanner, studentManager, gradeManager);

        assertThrows(StudentNotFoundException.class, action::execute);
    }

    @Test
    @DisplayName("Happy path: recording a Core subject grade succeeds and is persisted")
    void recordsCoreGradeSuccessfullyTest() {
        Student student = studentManager.getAllStudents().get(0);
        String studentId = student.getStudentId();

        String output = runWithInput(studentId + "\n1\n1\n85\nY\n\n");

        assertTrue(output.contains("GRADE CONFIRMATION"));
        assertTrue(output.contains("Grade recorded successfully"));
        List<Grade> grades = gradeManager.getGradesForStudent(studentId);
        assertEquals(1, grades.size());
        assertEquals(85.0, grades.get(0).getGrade(), 0.0001);
    }

    @Test
    @DisplayName("Happy path: recording an Elective subject grade succeeds and is persisted")
    void recordsElectiveGradeSuccessfullyTest() {
        Student student = studentManager.getAllStudents().get(0);
        String studentId = student.getStudentId();

        String output = runWithInput(studentId + "\n2\n1\n90\nY\n\n");

        assertTrue(output.contains("GRADE CONFIRMATION"));
        assertTrue(output.contains("Grade recorded successfully"));
        List<Grade> grades = gradeManager.getGradesForStudent(studentId);
        assertEquals(1, grades.size());
        assertEquals(90.0, grades.get(0).getGrade(), 0.0001);
    }

    private static Stream<Arguments> invalidOrDeclinedInputSuffixes() {
        // @MethodSource, not @CsvSource: these input suffixes contain literal
        // newlines, which @CsvSource's underlying parser reads as row
        // separators (corrupting the columns) rather than as part of one value.
        return Stream.of(
                Arguments.of("1\nabc\n", "Invalid selection."),
                Arguments.of("1\n99\n", "Invalid selection."),
                Arguments.of("1\n1\nabc\n", "Invalid grade."),
                Arguments.of("1\n1\n85\nN\n", "Grade recording cancelled.")
        );
    }

    @ParameterizedTest
    @MethodSource("invalidOrDeclinedInputSuffixes")
    @DisplayName("Each invalid/declined input path prints its matching message and records nothing")
    void invalidOrDeclinedInputRecordsNothingTest(String inputSuffix, String expectedMessage) {
        Student student = studentManager.getAllStudents().get(0);
        String studentId = student.getStudentId();

        String output = runWithInput(studentId + "\n" + inputSuffix);

        assertTrue(output.contains(expectedMessage));
        assertTrue(gradeManager.getGradesForStudent(studentId).isEmpty());
    }

    @Test
    @DisplayName("getOptionNumber(), getLabel() and isAuthorizedFor() report the expected menu metadata")
    void menuMetadataTest() {
        RecordGradeAction action = new RecordGradeAction(new Scanner(new ByteArrayInputStream(new byte[0])),
                studentManager, gradeManager);

        assertEquals(3, action.getOptionNumber());
        assertEquals("Record Grade", action.getLabel());
        assertTrue(action.isAuthorizedFor(Role.TEACHER));
        assertFalse(action.isAuthorizedFor(Role.STUDENT));
    }
}
