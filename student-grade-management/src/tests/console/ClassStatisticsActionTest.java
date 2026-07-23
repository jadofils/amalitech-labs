package tests.console;

import main.calculators.StatisticsCalculator;
import main.console.ClassStatisticsAction;
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
import java.util.List;
import java.util.Scanner;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Wires the real StudentServiceImpl/GradeManager stack (the same wiring
 * Main.java uses, mirroring StudentManagerTest's/ViewGradeReportActionTest's
 * @BeforeEach) to verify ClassStatisticsAction end-to-end.
 * ClassStatisticsActionMockitoTest verifies the same control flow purely
 * through mocked collaborators.
 */
class ClassStatisticsActionTest {

    private StudentRepositoryImpl studentRepository;
    private SubjectRepositoryImpl subjectRepository;
    private GradeManager gradeManager;
    private StudentManager studentManager;
    private StatisticsCalculator statisticsCalculator;

    @BeforeEach
    void setUp() {
        studentRepository = new StudentRepositoryImpl();
        subjectRepository = new SubjectRepositoryImpl();
        GradeService gradeService = new GradeServiceImpl(studentRepository, subjectRepository);
        gradeManager = new GradeManager(gradeService, subjectRepository);
        StudentService studentService = new StudentServiceImpl(studentRepository);
        studentManager = new StudentManager(studentService, gradeManager);
        statisticsCalculator = new StatisticsCalculator();
    }

    @Test
    @DisplayName("With no grades recorded, execute() reports the totals and returns early without any statistics section")
    void noGradesRecordedYetReturnsEarlyTest() {
        String output = runAction();

        assertTrue(output.contains("Total Students: 5"));
        assertTrue(output.contains("Total Grades Recorded: 0"));
        assertTrue(output.contains("No grades recorded yet."));
        assertFalse(output.contains("GRADE DISTRIBUTION"), "must return early before computing any statistics");
        assertFalse(output.contains("STATISTICAL ANALYSIS"));
        assertFalse(output.contains("SUBJECT PERFORMANCE"));
        assertFalse(output.contains("STUDENT TYPE COMPARISON"));
    }

    @Test
    @DisplayName("With grades recorded across Regular and Honors students, every statistics section is printed")
    void happyPathAllSectionsPrintedTest() {
        List<Student> students = studentRepository.getAllStudents();
        List<Subject> subjects = subjectRepository.getAllSubjects();
        // Alice (Regular), Carol (Regular), Emma (Regular) and Bob (Honors), David (Honors)
        // all get at least one grade, so both branches of the type comparison fire.
        gradeManager.addGrade(new Grade(students.get(0).getStudentId(), subjects.get(0), 92.0)); // Alice - Mathematics (A)
        gradeManager.addGrade(new Grade(students.get(0).getStudentId(), subjects.get(1), 78.0)); // Alice - English (B)
        gradeManager.addGrade(new Grade(students.get(1).getStudentId(), subjects.get(3), 65.0)); // Bob (Honors) - Music (C)
        gradeManager.addGrade(new Grade(students.get(2).getStudentId(), subjects.get(2), 45.0)); // Carol - Science (D)
        gradeManager.addGrade(new Grade(students.get(3).getStudentId(), subjects.get(4), 30.0)); // David (Honors) - Art (F)
        gradeManager.addGrade(new Grade(students.get(4).getStudentId(), subjects.get(5), 88.0)); // Emma - Phys Ed (A)

        String output = runAction();

        assertTrue(output.contains("Total Students: 5"));
        assertTrue(output.contains("Total Grades Recorded: 6"));

        assertTrue(output.contains("GRADE DISTRIBUTION"));

        assertTrue(output.contains("STATISTICAL ANALYSIS"));
        assertTrue(output.contains("Mean (Average):"));
        assertTrue(output.contains("Median:"));
        assertTrue(output.contains("Mode:"));
        assertTrue(output.contains("Standard Deviation:"));
        assertTrue(output.contains("Range:"));
        assertTrue(output.contains("Highest Grade:"));
        assertTrue(output.contains("Lowest Grade:"));

        assertTrue(output.contains("SUBJECT PERFORMANCE"));

        assertTrue(output.contains("STUDENT TYPE COMPARISON"));
        assertTrue(output.contains("Regular Students:"), "3 Regular students recorded a grade, so this line must print");
        assertTrue(output.contains("Honors Students:"), "both Honors students recorded a grade, so this line must print");
    }

    @Test
    @DisplayName("getOptionNumber() and getLabel() identify this as menu option 8")
    void menuIdentityTest() {
        ClassStatisticsAction action = new ClassStatisticsAction(scriptedScanner(), studentManager, gradeManager,
                statisticsCalculator, subjectRepository);

        assertEquals(8, action.getOptionNumber());
        assertEquals("View Class Statistics", action.getLabel());
    }

    private Scanner scriptedScanner() {
        return new Scanner(new ByteArrayInputStream("\n".getBytes(StandardCharsets.UTF_8)));
    }

    private String runAction() {
        ClassStatisticsAction action = new ClassStatisticsAction(scriptedScanner(), studentManager, gradeManager,
                statisticsCalculator, subjectRepository);
        PrintStream originalOut = System.out;
        ByteArrayOutputStream captured = new ByteArrayOutputStream();
        try {
            System.setOut(new PrintStream(captured, true, StandardCharsets.UTF_8));
            action.execute();
        } finally {
            System.setOut(originalOut);
        }
        return captured.toString(StandardCharsets.UTF_8);
    }
}
