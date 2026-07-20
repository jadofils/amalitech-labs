package tests.manager;

import manager.GradeManager;
import manager.StudentManager;
import model.grade.Grade;
import model.student.HonorsStudent;
import model.student.RegularStudent;
import model.student.Student;
import model.subject.Subject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import repository.student.StudentRepositoryImpl;
import repository.subject.SubjectRepositoryImpl;
import service.GradeService;
import service.StudentService;
import service.GradeServiceImpl;
import service.StudentServiceImpl;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Wires the real StudentServiceImpl/GradeManager stack (the same wiring
 * Main.java uses) to verify hydration end-to-end. StudentManagerMockitoTest
 * verifies the same hydration logic purely through mocked collaborators.
 */
class StudentManagerTest {

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
    @DisplayName("A freshly-seeded student has no grades until one is recorded")
    void freshlySeededStudentHasNoGradesTest() {
        Student student = studentManager.getAllStudents().get(0);

        Student hydrated = studentManager.findStudent(student.getStudentId());

        assertTrue(hydrated.getGrades().isEmpty());
        assertEquals(0.0, hydrated.calculateAverageGrade());
    }

    @Test
    @DisplayName("findStudent() reflects grades recorded via GradeManager")
    void findStudentReflectsRecordedGradesTest() {
        Student student = studentManager.getAllStudents().get(0);
        Subject subject = subjectRepository.getAllSubjects().get(0);
        gradeManager.addGrade(new Grade(student.getStudentId(), subject, 80.0));

        Student hydrated = studentManager.findStudent(student.getStudentId());

        assertEquals(1, hydrated.getGrades().size());
        assertEquals(80.0, hydrated.calculateAverageGrade());
    }

    @Test
    @DisplayName("findStudent() returns null (not an exception) for an unknown ID")
    void findStudentUnknownReturnsNullTest() {
        assertNull(studentManager.findStudent("NOPE"));
    }

    @Test
    @DisplayName("getAllStudents() hydrates grades for every student, not just the first")
    void getAllStudentsHydratesEveryoneTest() {
        List<Student> students = studentRepository.getAllStudents();
        Subject subject = subjectRepository.getAllSubjects().get(0);
        gradeManager.addGrade(new Grade(students.get(0).getStudentId(), subject, 80.0));
        gradeManager.addGrade(new Grade(students.get(2).getStudentId(), subject, 60.0));

        List<Student> hydrated = studentManager.getAllStudents();

        assertEquals(1, hydrated.get(0).getGrades().size());
        assertTrue(hydrated.get(1).getGrades().isEmpty());
        assertEquals(1, hydrated.get(2).getGrades().size());
    }

    @Test
    @DisplayName("getAverageClassGrade() averages hydrated per-student averages")
    void getAverageClassGradeTest() {
        Student first = studentRepository.getAllStudents().get(0);
        Subject subject = subjectRepository.getAllSubjects().get(0);
        gradeManager.addGrade(new Grade(first.getStudentId(), subject, 100.0));
        // The other 4 seeded students have no grades, i.e. an average of 0.0 each.

        double classAverage = studentManager.getAverageClassGrade();

        assertEquals(20.0, classAverage, 0.0001); // (100 + 0 + 0 + 0 + 0) / 5
    }

    @Test
    @DisplayName("getStudentCount() increases after addStudent()")
    void getStudentCountTest() {
        int before = studentManager.getStudentCount();

        studentManager.addStudent(new RegularStudent("New Student", 16, "new@school.edu", "1234567890"));

        assertEquals(before + 1, studentManager.getStudentCount());
    }

    @Test
    @DisplayName("Honors eligibility is recomputed to reflect newly recorded grades")
    void honorsEligibilityReflectsNewGradesTest() {
        Student honorsStudent = studentRepository.getAllStudents().stream()
                .filter(s -> s instanceof HonorsStudent)
                .findFirst()
                .orElseThrow();
        Subject subject = subjectRepository.getAllSubjects().get(0);

        // Bring the average below the 60% threshold used by the current
        // implementation (see Lab 2's technical documentation).
        gradeManager.addGrade(new Grade(honorsStudent.getStudentId(), subject, 30.0));
        HonorsStudent hydrated = (HonorsStudent) studentManager.findStudent(honorsStudent.getStudentId());
        assertFalse(hydrated.checkHonorsEligibility());

        gradeManager.addGrade(new Grade(honorsStudent.getStudentId(), subject, 100.0)); // average now 65.0
        HonorsStudent rehydrated = (HonorsStudent) studentManager.findStudent(honorsStudent.getStudentId());
        assertTrue(rehydrated.checkHonorsEligibility());
    }
}
