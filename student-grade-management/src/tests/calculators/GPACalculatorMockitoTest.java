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

import java.util.List;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Mocks GradeManager and StudentManager to verify GPACalculator's own
 * arithmetic in isolation from their real implementations.
 */
class GPACalculatorMockitoTest {

    private final Subject subject = new CoreSubject("Mathematics", "MATH01");

    @Test
    @DisplayName("cumulativeGPA() delegates to GradeManager.getGradesForStudent() exactly once")
    void cumulativeGPADelegatesTest() {
        GradeManager gradeManager = mock(GradeManager.class);
        StudentManager studentManager = mock(StudentManager.class);
        GPACalculator calculator = new GPACalculator(gradeManager, studentManager);
        List<Grade> grades = List.of(new Grade("STU001", subject, 95.0));
        when(gradeManager.getGradesForStudent("STU001")).thenReturn(grades);

        double result = calculator.cumulativeGPA("STU001");

        assertEquals(4.0, result, 0.0001);
        verify(gradeManager, times(1)).getGradesForStudent("STU001");
    }

    @Test
    @DisplayName("classRank() treats an unknown student's average as 0.0 rather than throwing")
    void classRankUnknownStudentTreatedAsZeroTest() {
        GradeManager gradeManager = mock(GradeManager.class);
        StudentManager studentManager = mock(StudentManager.class);
        GPACalculator calculator = new GPACalculator(gradeManager, studentManager);
        when(studentManager.findStudent("NOPE")).thenReturn(null);
        Student other = new RegularStudent("Other Student", 16, "other@school.edu", "1234567890");
        other.addGrade(50.0);
        when(studentManager.getAllStudents()).thenReturn(List.of(other));

        int rank = calculator.classRank("NOPE");

        // "NOPE" has an implied average of 0.0; the one real student (50.0)
        // outranks it, so "NOPE" is rank 2.
        assertEquals(2, rank);
    }

    @Test
    @DisplayName("classRank() ranks a student ahead of everyone with a lower average")
    void classRankOrdersByAverageTest() {
        GradeManager gradeManager = mock(GradeManager.class);
        StudentManager studentManager = mock(StudentManager.class);
        GPACalculator calculator = new GPACalculator(gradeManager, studentManager);
        Student top = new RegularStudent("Top Student", 16, "top@school.edu", "1234567890");
        top.addGrade(99.0);
        Student middle = new RegularStudent("Middle Student", 16, "middle@school.edu", "1234567890");
        middle.addGrade(70.0);
        when(studentManager.findStudent(top.getStudentId())).thenReturn(top);
        when(studentManager.getAllStudents()).thenReturn(List.of(top, middle));

        assertEquals(1, calculator.classRank(top.getStudentId()));
    }
}
