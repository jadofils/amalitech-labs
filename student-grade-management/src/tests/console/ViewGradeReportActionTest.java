package tests.console;

import main.console.ViewGradeReportAction;
import main.exceptions.StudentNotFoundException;
import main.manager.GradeManager;
import main.manager.StudentManager;
import main.model.grade.Grade;
import main.model.student.Student;
import main.model.subject.Subject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import main.repository.student.StudentRepositoryImpl;
import main.repository.subject.SubjectRepositoryImpl;
import main.service.GradeService;
import main.service.GradeServiceImpl;
import main.service.StudentService;
import main.service.StudentServiceImpl;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Wires the real StudentServiceImpl/GradeManager stack (the same wiring
 * Main.java uses, mirroring StudentManagerTest's @BeforeEach) to verify
 * ViewGradeReportAction end-to-end. ViewGradeReportActionMockitoTest verifies
 * the same control flow purely through mocked collaborators.
 */
class ViewGradeReportActionTest {

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

    @Test
    @DisplayName("execute() throws StudentNotFoundException for an unknown student ID")
    void executeUnknownStudentThrowsTest() {
        Scanner scanner = scannerFor("NOPE\n");
        ViewGradeReportAction action = new ViewGradeReportAction(scanner, studentManager, gradeManager);

        assertThrows(StudentNotFoundException.class, () -> captureStdOut(action::execute));
    }

    @Test
    @DisplayName("execute() prints the student header, passing grade, and delegated grade history when grades exist")
    void executeWithGradesTest() {
        Student student = studentManager.getAllStudents().get(0);
        Subject subject = subjectRepository.getAllSubjects().get(0);
        gradeManager.addGrade(new Grade(student.getStudentId(), subject, 85.0));
        Scanner scanner = scannerFor(student.getStudentId() + "\n\n");
        ViewGradeReportAction action = new ViewGradeReportAction(scanner, studentManager, gradeManager);

        String output = captureStdOut(action::execute);

        assertTrue(output.contains(student.getName()));
        assertTrue(output.contains("Type: " + student.getStudentType() + " Student"));
        assertTrue(output.contains(String.format("Passing Grade: %.0f%%", student.getPassingGrade())));
        assertTrue(output.contains("GRADE HISTORY"));
    }

    @Test
    @DisplayName("execute() prints the student header and the no-grades message when none are recorded")
    void executeWithoutGradesTest() {
        Student student = studentManager.getAllStudents().get(1);
        Scanner scanner = scannerFor(student.getStudentId() + "\n\n");
        ViewGradeReportAction action = new ViewGradeReportAction(scanner, studentManager, gradeManager);

        String output = captureStdOut(action::execute);

        assertTrue(output.contains(student.getName()));
        assertTrue(output.contains("No grades recorded for this student."));
    }

    @Test
    @DisplayName("getOptionNumber() and getLabel() identify this as menu option 4")
    void menuIdentityTest() {
        ViewGradeReportAction action = new ViewGradeReportAction(scannerFor(""), studentManager, gradeManager);

        assertEquals(4, action.getOptionNumber());
        assertEquals("View Grade Report", action.getLabel());
    }

    private Scanner scannerFor(String input) {
        return new Scanner(new ByteArrayInputStream(input.getBytes(StandardCharsets.UTF_8)));
    }

    private String captureStdOut(Runnable action) {
        PrintStream original = System.out;
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        System.setOut(new PrintStream(buffer, true, StandardCharsets.UTF_8));
        try {
            action.run();
        } finally {
            System.setOut(original);
        }
        return buffer.toString(StandardCharsets.UTF_8);
    }
}
