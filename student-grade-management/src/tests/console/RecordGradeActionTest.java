package tests.console;

import console.RecordGradeAction;
import exceptions.StudentNotFoundException;
import manager.GradeManager;
import manager.StudentManager;
import model.enums.Role;
import model.grade.Grade;
import model.student.Student;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import repository.student.StudentRepositoryImpl;
import repository.subject.SubjectRepositoryImpl;
import service.GradeService;
import service.StudentService;
import service.GradeServiceImpl;
import service.StudentServiceImpl;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Scanner;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Wires the real StudentManager/GradeManager stack (the same wiring Main.java
 * uses) to verify RecordGradeAction's happy paths and validation branches
 * end-to-end. RecordGradeActionMockitoTest verifies the same behavior purely
 * through mocked collaborators, including the branch (no subjects available
 * for a type) that the real seeded repository can never actually produce.
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
        Scanner scanner = new Scanner(new ByteArrayInputStream(scriptedInput.getBytes(StandardCharsets.UTF_8)));
        PrintStream originalOut = System.out;
        ByteArrayOutputStream captured = new ByteArrayOutputStream();
        try {
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

    @Test
    @DisplayName("Invalid (non-numeric) subject selection prints 'Invalid selection.' and records nothing")
    void invalidSubjectSelectionNonNumericTest() {
        Student student = studentManager.getAllStudents().get(0);
        String studentId = student.getStudentId();

        String output = runWithInput(studentId + "\n1\nabc\n");

        assertTrue(output.contains("Invalid selection."));
        assertTrue(gradeManager.getGradesForStudent(studentId).isEmpty());
    }

    @Test
    @DisplayName("Out-of-range subject selection number prints 'Invalid selection.'")
    void outOfRangeSubjectSelectionTest() {
        Student student = studentManager.getAllStudents().get(0);
        String studentId = student.getStudentId();

        String output = runWithInput(studentId + "\n1\n99\n");

        assertTrue(output.contains("Invalid selection."));
        assertTrue(gradeManager.getGradesForStudent(studentId).isEmpty());
    }

    @Test
    @DisplayName("Invalid (non-numeric) grade input prints 'Invalid grade.'")
    void invalidGradeInputTest() {
        Student student = studentManager.getAllStudents().get(0);
        String studentId = student.getStudentId();

        String output = runWithInput(studentId + "\n1\n1\nabc\n");

        assertTrue(output.contains("Invalid grade."));
        assertTrue(gradeManager.getGradesForStudent(studentId).isEmpty());
    }

    @Test
    @DisplayName("Declining the confirmation prints 'Grade recording cancelled.' and records nothing")
    void declinedConfirmationCancelsTest() {
        Student student = studentManager.getAllStudents().get(0);
        String studentId = student.getStudentId();

        String output = runWithInput(studentId + "\n1\n1\n85\nN\n");

        assertTrue(output.contains("Grade recording cancelled."));
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
