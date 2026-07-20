package tests.calculators;

import calculators.StatisticsCalculator;
import model.enums.SubjectType;
import model.grade.Grade;
import model.subject.Subject;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Mocks Grade/Subject so StatisticsCalculator's own arithmetic is verified
 * independent of the real Grade/Subject implementations.
 */
class StatisticsCalculatorMockitoTest {

    private final StatisticsCalculator calculator = new StatisticsCalculator();

    private Grade mockGrade(String studentId, double value, Subject subject) {
        Grade grade = mock(Grade.class);
        when(grade.getGrade()).thenReturn(value);
        when(grade.getStudentId()).thenReturn(studentId);
        when(grade.getSubject()).thenReturn(subject);
        return grade;
    }

    private Subject mockSubject(String name, String code, SubjectType type) {
        Subject subject = mock(Subject.class);
        when(subject.getSubjectName()).thenReturn(name);
        when(subject.getSubjectCode()).thenReturn(code);
        when(subject.getSubjectType()).thenReturn(type);
        return subject;
    }

    @Test
    @DisplayName("calculateSubjectAverages() matches grades to subjects by subject code, not identity")
    void calculateSubjectAveragesMatchesByCodeTest() {
        Subject math = mockSubject("Mathematics", "MATH01", SubjectType.CORE);
        List<Grade> grades = List.of(
                mockGrade("STU001", 80.0, math),
                mockGrade("STU002", 100.0, math));

        List<StatisticsCalculator.SubjectAverage> averages =
                calculator.calculateSubjectAverages(grades, List.of(math));

        assertEquals(1, averages.size());
        assertEquals(90.0, averages.get(0).getAverage(), 0.0001);
        verify(math, atLeastOnce()).getSubjectCode();
    }

    @Test
    @DisplayName("calculateDistribution() reads only getGrade() from each Grade, nothing else")
    void calculateDistributionOnlyReadsGradeValueTest() {
        Grade grade = mockGrade("STU001", 90.0, null);

        calculator.calculateDistribution(List.of(grade));

        verify(grade, atLeastOnce()).getGrade();
        verify(grade, never()).getStudentId();
        verify(grade, never()).getSubject();
    }
}
