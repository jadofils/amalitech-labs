package tests.console;

import main.console.ViewStudentsAction;
import main.manager.GradeManager;
import main.manager.StudentManager;
import main.model.enums.Role;
import main.model.grade.Grade;
import main.model.student.HonorsStudent;
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
 * Wires the real StudentManager/StudentServiceImpl/GradeManager stack (the
 * same wiring Main.java uses, mirroring StudentManagerTest's @BeforeEach) to
 * verify ViewStudentsAction's main.console output end-to-end.
 * ViewStudentsActionMockitoTest verifies the same class purely through a
 * mocked StudentManager.
 */
class ViewStudentsActionTest {

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
    @DisplayName("Fresh listing shows all 5 seeded students as Failing with a 0.0% class average")
    void freshListingShowsAllFiveStudentsFailingTest() {
        String output = runAction();

        for (Student seeded : studentRepository.getAllStudents()) {
            assertTrue(output.contains(seeded.getStudentId()), "missing ID for " + seeded.getName());
            assertTrue(output.contains(seeded.getName()), "missing name " + seeded.getName());
        }
        assertEquals(5, countOccurrences(output, "Failing"));
        assertTrue(output.contains("Total Students: 5"));
        assertTrue(output.contains("Average Class Grade: 0.0%"));
        assertFalse(output.contains("Honors Eligible"), "no seeded student has grades yet, so none can be honors-eligible");
    }

    @Test
    @DisplayName("Recording a grade for one student updates only that student's average and passing status")
    void recordedGradeUpdatesOnlyThatStudentTest() {
        Student regular = studentRepository.getAllStudents().get(0); // Alice Johnson, Regular
        Subject subject = subjectRepository.getAllSubjects().get(0);
        gradeManager.addGrade(new Grade(regular.getStudentId(), subject, 80.0));

        String output = runAction();

        String regularLine = lineContaining(output, regular.getName());
        assertTrue(regularLine.contains("80.0%"));
        assertTrue(regularLine.contains("Passing"));
        assertEquals(4, countOccurrences(output, "Failing"), "the other 4 seeded students remain ungraded/failing");
        assertTrue(output.contains("Total Students: 5"));
        assertTrue(output.contains("Average Class Grade: 16.0%")); // (80 + 0 + 0 + 0 + 0) / 5
    }

    @Test
    @DisplayName("Honors student's detail row shows Honors Eligible once their average meets the honors threshold")
    void honorsStudentEligibleRowTest() {
        Student honors = studentRepository.getAllStudents().stream()
                .filter(s -> s instanceof HonorsStudent)
                .findFirst()
                .orElseThrow();
        Subject subject = subjectRepository.getAllSubjects().get(0);
        gradeManager.addGrade(new Grade(honors.getStudentId(), subject, 90.0)); // >= 60% honors threshold

        String output = runAction();

        String detailLine = lineAfter(output, honors.getName());
        assertTrue(detailLine.contains("Enrolled Subjects: 1"));
        assertTrue(detailLine.contains("Honors Eligible"));
    }

    @Test
    @DisplayName("Regular student's row has no honors fields; a not-yet-eligible Honors student's row omits Honors Eligible")
    void regularRowHasNoHonorsFieldsAndIneligibleHonorsRowOmitsItTest() {
        String output = runAction();

        Student regular = studentRepository.getAllStudents().stream()
                .filter(s -> !(s instanceof HonorsStudent))
                .findFirst()
                .orElseThrow();
        Student honors = studentRepository.getAllStudents().stream()
                .filter(s -> s instanceof HonorsStudent)
                .findFirst()
                .orElseThrow();

        String regularDetail = lineAfter(output, regular.getName());
        assertTrue(regularDetail.contains("Enrolled Subjects: 0"));
        assertFalse(regularDetail.contains("Honors Eligible"));

        // Fresh Honors student has a 0.0% average, below the 60% threshold, so not eligible yet.
        String honorsDetail = lineAfter(output, honors.getName());
        assertTrue(honorsDetail.contains("Enrolled Subjects: 0"));
        assertFalse(honorsDetail.contains("Honors Eligible"));
    }

    @Test
    @DisplayName("Menu metadata: option 2, 'View Students', authorized for TEACHER only")
    void menuMetadataTest() {
        ViewStudentsAction action = new ViewStudentsAction(scriptedScanner(), studentManager);

        assertEquals(2, action.getOptionNumber());
        assertEquals("View Students", action.getLabel());
        assertTrue(action.isAuthorizedFor(Role.TEACHER));
        assertFalse(action.isAuthorizedFor(Role.STUDENT));
    }

    private Scanner scriptedScanner() {
        return new Scanner(new ByteArrayInputStream("\n".getBytes(StandardCharsets.UTF_8)));
    }

    private String runAction() {
        ViewStudentsAction action = new ViewStudentsAction(scriptedScanner(), studentManager);
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

    private int countOccurrences(String haystack, String needle) {
        int count = 0, index = 0;
        while ((index = haystack.indexOf(needle, index)) != -1) {
            count++;
            index += needle.length();
        }
        return count;
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
