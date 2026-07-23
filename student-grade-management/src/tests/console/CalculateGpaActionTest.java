package tests.console;

import main.console.CalculateGpaAction;
import main.calculators.GPACalculator;
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
import main.service.StudentService;
import main.service.GradeServiceImpl;
import main.service.StudentServiceImpl;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Wires the real StudentManager/GradeManager/GPACalculator stack (the same
 * wiring Main.java uses) to verify CalculateGpaAction's happy path and
 * performance/class-average branches end-to-end.
 * CalculateGpaActionMockitoTest verifies the same branching purely through
 * mocked collaborators, including combinations (honors eligibility,
 * above/below class average) that are awkward to force through the real
 * seeded main.repository.
 */
class CalculateGpaActionTest {

    private StudentRepositoryImpl studentRepository;
    private SubjectRepositoryImpl subjectRepository;
    private GradeManager gradeManager;
    private StudentManager studentManager;
    private GPACalculator gpaCalculator;

    @BeforeEach
    void setUp() {
        studentRepository = new StudentRepositoryImpl();
        subjectRepository = new SubjectRepositoryImpl();
        GradeService gradeService = new GradeServiceImpl(studentRepository, subjectRepository);
        gradeManager = new GradeManager(gradeService, subjectRepository);
        StudentService studentService = new StudentServiceImpl(studentRepository);
        studentManager = new StudentManager(studentService, gradeManager);
        gpaCalculator = new GPACalculator(gradeManager, studentManager);
    }

    private String runWithInput(String scriptedInput) {
        Scanner scanner = new Scanner(new ByteArrayInputStream(scriptedInput.getBytes(StandardCharsets.UTF_8)));
        PrintStream originalOut = System.out;
        ByteArrayOutputStream captured = new ByteArrayOutputStream();
        try {
            System.setOut(new PrintStream(captured, true, StandardCharsets.UTF_8));
            new CalculateGpaAction(scanner, studentManager, gradeManager, gpaCalculator).execute();
        } finally {
            System.setOut(originalOut);
        }
        return captured.toString(StandardCharsets.UTF_8);
    }

    @Test
    @DisplayName("execute() throws StudentNotFoundException when the student ID isn't found")
    void studentNotFoundThrowsTest() {
        Scanner scanner = new Scanner(new ByteArrayInputStream("NOPE\n".getBytes(StandardCharsets.UTF_8)));
        CalculateGpaAction action = new CalculateGpaAction(scanner, studentManager, gradeManager, gpaCalculator);

        assertThrows(StudentNotFoundException.class, action::execute);
    }

    @Test
    @DisplayName("Happy path: recorded grades produce a full GPA report with an excellent-performance, above-average result")
    void happyPathExcellentPerformanceAboveClassAverageTest() {
        Student alice = studentRepository.getAllStudents().get(0); // RegularStudent
        String studentId = alice.getStudentId();
        Subject math = subjectRepository.getAllSubjects().get(0); // CoreSubject "Mathematics"
        Subject english = subjectRepository.getAllSubjects().get(1); // CoreSubject "English"
        gradeManager.addGrade(new Grade(studentId, math, 95.0)); // GPA 4.0
        gradeManager.addGrade(new Grade(studentId, english, 85.0)); // GPA 3.0
        // cumulativeGPA = (4.0 + 3.0) / 2 = 3.5

        String output = runWithInput(studentId + "\n\n");

        assertTrue(output.contains("GPA CALCULATION"));
        assertTrue(output.contains("Mathematics"));
        assertTrue(output.contains("4.0 (A)"));
        assertTrue(output.contains("English"));
        assertTrue(output.contains("3.0 (B)"));
        assertTrue(output.contains("Cumulative GPA: 3.50 / 4.0"));
        assertTrue(output.contains("Letter Grade: B+"));
        assertTrue(output.contains("Class Rank:"));
        assertTrue(output.contains("Excellent performance (3.5+ GPA)"));
        assertTrue(output.contains("Above class average"));
        assertFalse(output.contains("Honors eligibility maintained"), "a RegularStudent must never show the honors line");
    }

    @Test
    @DisplayName("A low grade produces a needs-improvement, below-class-average result")
    void lowGradeNeedsImprovementBelowClassAverageTest() {
        Student carol = studentRepository.getAllStudents().get(2); // RegularStudent
        String studentId = carol.getStudentId();
        Subject math = subjectRepository.getAllSubjects().get(0);
        gradeManager.addGrade(new Grade(studentId, math, 50.0)); // GPA 0.0

        String output = runWithInput(studentId + "\n\n");

        assertTrue(output.contains("Cumulative GPA: 0.00 / 4.0"));
        assertTrue(output.contains("Letter Grade: F"));
        assertTrue(output.contains("Needs improvement (below 2.0 GPA)"));
        assertTrue(output.contains("Below class average"));
    }

    @Test
    @DisplayName("An eligible honors student sees the honors-eligibility-maintained line")
    void honorsEligibleStudentSeesHonorsLineTest() {
        Student bob = studentRepository.getAllStudents().get(1); // HonorsStudent
        String studentId = bob.getStudentId();
        Subject math = subjectRepository.getAllSubjects().get(0);
        gradeManager.addGrade(new Grade(studentId, math, 90.0)); // average 90 >= 60 passing grade

        String output = runWithInput(studentId + "\n\n");

        assertTrue(output.contains("Honors eligibility maintained"));
    }

    @Test
    @DisplayName("getOptionNumber() and getLabel() report the expected menu metadata")
    void menuMetadataTest() {
        CalculateGpaAction action = new CalculateGpaAction(new Scanner(new ByteArrayInputStream(new byte[0])),
                studentManager, gradeManager, gpaCalculator);

        assertEquals(6, action.getOptionNumber());
        assertEquals("Calculate Student GPA", action.getLabel());
    }
}
