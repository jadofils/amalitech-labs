package tests.calculators;

import main.calculators.GPACalculator;
import main.manager.GradeManager;
import main.manager.StudentManager;
import main.model.grade.Grade;
import main.model.student.RegularStudent;
import main.model.student.Student;
import main.model.subject.CoreSubject;
import main.model.subject.Subject;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import main.repository.student.StudentRepositoryImpl;
import main.repository.subject.SubjectRepositoryImpl;
import main.service.GradeService;
import main.service.StudentService;
import main.service.GradeServiceImpl;
import main.service.StudentServiceImpl;

import static org.junit.jupiter.api.Assertions.*;

class GPACalculatorTest {

    private final Subject subject = new CoreSubject("Mathematics", "MATH01");

    private GPACalculator newCalculator(StudentRepositoryImpl students, SubjectRepositoryImpl subjects,
                                         GradeManager gradeManager) {
        StudentService studentService = new StudentServiceImpl(students);
        StudentManager studentManager = new StudentManager(studentService, gradeManager);
        return new GPACalculator(gradeManager, studentManager);
    }

    // ReadMe-v2.md's own grading table, row for row.
    @ParameterizedTest
    @CsvSource({
            "100, 4.0", "93, 4.0",
            "92, 3.7", "90, 3.7",
            "89, 3.3", "87, 3.3",
            "86, 3.0", "83, 3.0",
            "82, 2.7", "80, 2.7",
            "79, 2.3", "77, 2.3",
            "76, 2.0", "73, 2.0",
            "72, 1.7", "70, 1.7",
            "69, 1.3", "67, 1.3",
            "66, 1.0", "60, 1.0",
            "59, 0.0", "0, 0.0"
    })
    @DisplayName("percentageToGPA() matches the ReadMe-v2.md grading table")
    void percentageToGPAMatchesTableTest(double percentage, double expectedGPA) {
        StudentRepositoryImpl students = new StudentRepositoryImpl();
        SubjectRepositoryImpl subjects = new SubjectRepositoryImpl();
        GradeService gradeService = new GradeServiceImpl(students, subjects);
        GradeManager gradeManager = new GradeManager(gradeService, subjects);
        GPACalculator calculator = newCalculator(students, subjects, gradeManager);

        assertEquals(expectedGPA, calculator.percentageToGPA(percentage), 0.0001);
    }

    // Regression test for CHANGELOG.md KI-1: gpaToLetter() used to return the
    // letter belonging to the *next tier up* for every value except 4.0 and
    // 0.0 (e.g. 3.7 produced "A" instead of "A-"). Every GPA value below
    // comes directly from the table, paired with its documented letter.
    @ParameterizedTest
    @CsvSource({
            "4.0, A", "3.7, A-", "3.3, B+", "3.0, B", "2.7, B-",
            "2.3, C+", "2.0, C", "1.7, C-", "1.3, D+", "1.0, D", "0.0, F"
    })
    @DisplayName("gpaToLetter() matches the ReadMe-v2.md grading table for every documented GPA value")
    void gpaToLetterMatchesTableTest(double gpa, String expectedLetter) {
        StudentRepositoryImpl students = new StudentRepositoryImpl();
        SubjectRepositoryImpl subjects = new SubjectRepositoryImpl();
        GradeService gradeService = new GradeServiceImpl(students, subjects);
        GradeManager gradeManager = new GradeManager(gradeService, subjects);
        GPACalculator calculator = newCalculator(students, subjects, gradeManager);

        assertEquals(expectedLetter, calculator.gpaToLetter(gpa));
    }

    @Test
    @DisplayName("cumulativeGPA() averages the GPA points of every grade recorded for that student")
    void cumulativeGPAAveragesGradesTest() {
        StudentRepositoryImpl students = new StudentRepositoryImpl();
        SubjectRepositoryImpl subjects = new SubjectRepositoryImpl();
        GradeService gradeService = new GradeServiceImpl(students, subjects);
        GradeManager gradeManager = new GradeManager(gradeService, subjects);
        GPACalculator calculator = newCalculator(students, subjects, gradeManager);
        Student student = students.getAllStudents().get(0);

        gradeManager.addGrade(new Grade(student.getStudentId(), subject, 95.0)); // 4.0
        gradeManager.addGrade(new Grade(student.getStudentId(), subject, 85.0)); // 3.0

        assertEquals(3.5, calculator.cumulativeGPA(student.getStudentId()), 0.0001);
    }

    @Test
    @DisplayName("cumulativeGPA() is 0.0 for a student with no grades")
    void cumulativeGPANoGradesTest() {
        StudentRepositoryImpl students = new StudentRepositoryImpl();
        SubjectRepositoryImpl subjects = new SubjectRepositoryImpl();
        GradeService gradeService = new GradeServiceImpl(students, subjects);
        GradeManager gradeManager = new GradeManager(gradeService, subjects);
        GPACalculator calculator = newCalculator(students, subjects, gradeManager);
        Student student = students.getAllStudents().get(0);

        assertEquals(0.0, calculator.cumulativeGPA(student.getStudentId()));
    }

    @Test
    @DisplayName("classRank() ranks the highest average as 1")
    void classRankOrdersByAverageTest() {
        StudentRepositoryImpl students = new StudentRepositoryImpl();
        SubjectRepositoryImpl subjects = new SubjectRepositoryImpl();
        GradeService gradeService = new GradeServiceImpl(students, subjects);
        GradeManager gradeManager = new GradeManager(gradeService, subjects);
        GPACalculator calculator = newCalculator(students, subjects, gradeManager);

        Student top = new RegularStudent("Top Student", 16, "top@school.edu", "1234567890");
        Student middle = new RegularStudent("Middle Student", 16, "middle@school.edu", "1234567890");
        students.addStudent(top);
        students.addStudent(middle);
        gradeManager.addGrade(new Grade(top.getStudentId(), subject, 99.0));
        gradeManager.addGrade(new Grade(middle.getStudentId(), subject, 70.0));

        assertEquals(1, calculator.classRank(top.getStudentId()));
        assertTrue(calculator.classRank(middle.getStudentId()) > 1);
    }
}
