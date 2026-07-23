package tests.manager;

import main.manager.GradeManager;
import main.model.enums.SubjectType;
import main.model.grade.Grade;
import main.model.student.RegularStudent;
import main.model.student.Student;
import main.model.subject.Subject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import main.repository.student.StudentRepositoryImpl;
import main.repository.subject.SubjectRepositoryImpl;
import main.service.GradeService;
import main.service.GradeServiceImpl;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Wires GradeManager to a real GradeServiceImpl and real repositories (the
 * same wiring Main.java uses). GradeManagerMockitoTest verifies the same
 * arithmetic/filtering logic purely through mocked collaborators.
 */
class GradeManagerTest {

    private StudentRepositoryImpl studentRepository;
    private SubjectRepositoryImpl subjectRepository;
    private GradeManager gradeManager;

    @BeforeEach
    void setUp() {
        studentRepository = new StudentRepositoryImpl();
        subjectRepository = new SubjectRepositoryImpl();
        GradeService gradeService = new GradeServiceImpl(studentRepository, subjectRepository);
        gradeManager = new GradeManager(gradeService, subjectRepository);
    }

    @Test
    @DisplayName("getSubjectsByType() returns exactly the 3 seeded core subjects")
    void getCoreSubjectsTest() {
        List<Subject> core = gradeManager.getSubjectsByType(SubjectType.CORE);
        assertEquals(3, core.size());
        assertTrue(core.stream().allMatch(s -> s.getSubjectType() == SubjectType.CORE));
    }

    @Test
    @DisplayName("getSubjectsByType() returns exactly the 3 seeded elective subjects")
    void getElectiveSubjectsTest() {
        List<Subject> electives = gradeManager.getSubjectsByType(SubjectType.ELECTIVE);
        assertEquals(3, electives.size());
        assertTrue(electives.stream().allMatch(s -> s.getSubjectType() == SubjectType.ELECTIVE));
    }

    @Test
    @DisplayName("Core/elective/overall averages are correct for a mix of grades")
    void averagesWithMixedGradesTest() {
        Student student = studentRepository.getAllStudents().get(0);
        String studentId = student.getStudentId();
        Subject core1 = gradeManager.getSubjectsByType(SubjectType.CORE).get(0);
        Subject core2 = gradeManager.getSubjectsByType(SubjectType.CORE).get(1);
        Subject elective = gradeManager.getSubjectsByType(SubjectType.ELECTIVE).get(0);

        gradeManager.addGrade(new Grade(studentId, core1, 80.0));
        gradeManager.addGrade(new Grade(studentId, core2, 90.0));
        gradeManager.addGrade(new Grade(studentId, elective, 70.0));

        assertEquals(85.0, gradeManager.calculateCoreAverage(studentId), 0.0001);
        assertEquals(70.0, gradeManager.calculateElectiveAverage(studentId), 0.0001);
        assertEquals(80.0, gradeManager.calculateOverallAverage(studentId), 0.0001);
    }

    @Test
    @DisplayName("Averages are 0.0 (not an exception) for a student with no grades")
    void averagesWithNoGradesTest() {
        Student student = new RegularStudent("No Grades Yet", 16, "none@school.edu", "1234567890");
        studentRepository.addStudent(student);

        assertEquals(0.0, gradeManager.calculateCoreAverage(student.getStudentId()));
        assertEquals(0.0, gradeManager.calculateElectiveAverage(student.getStudentId()));
        assertEquals(0.0, gradeManager.calculateOverallAverage(student.getStudentId()));
    }

    @Test
    @DisplayName("getGradeCount() reflects the total number of recorded grades")
    void getGradeCountTest() {
        Student student = studentRepository.getAllStudents().get(0);
        Subject subject = gradeManager.getSubjectsByType(SubjectType.CORE).get(0);
        assertEquals(0, gradeManager.getGradeCount());

        gradeManager.addGrade(new Grade(student.getStudentId(), subject, 80.0));
        gradeManager.addGrade(new Grade(student.getStudentId(), subject, 90.0));

        assertEquals(2, gradeManager.getGradeCount());
    }

    @Test
    @DisplayName("viewGradesByStudent() prints a specific message when there are no grades")
    void viewGradesByStudentEmptyTest() {
        Student student = studentRepository.getAllStudents().get(0);
        String output = captureStdOut(() -> gradeManager.viewGradesByStudent(student.getStudentId()));
        assertTrue(output.contains("No grades recorded for this student."));
    }

    @Test
    @DisplayName("viewGradesByStudent() lists grades newest-first (by ID, since all share today's date)")
    void viewGradesByStudentOrderingTest() {
        Student student = studentRepository.getAllStudents().get(0);
        Subject subject = gradeManager.getSubjectsByType(SubjectType.CORE).get(0);
        Grade first = new Grade(student.getStudentId(), subject, 80.0);
        Grade second = new Grade(student.getStudentId(), subject, 90.0);
        gradeManager.addGrade(first);
        gradeManager.addGrade(second);

        String output = captureStdOut(() -> gradeManager.viewGradesByStudent(student.getStudentId()));

        assertTrue(output.indexOf(second.getGradeId()) < output.indexOf(first.getGradeId()));
    }

    private String captureStdOut(Runnable action) {
        PrintStream original = System.out;
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        System.setOut(new PrintStream(buffer));
        try {
            action.run();
        } finally {
            System.setOut(original);
        }
        return buffer.toString();
    }
}
