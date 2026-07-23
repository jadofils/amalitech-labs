package tests.console;

import main.calculators.GPACalculator;
import main.console.CalculateGpaAction;
import main.exceptions.StudentNotFoundException;
import main.manager.GradeManager;
import main.manager.StudentManager;
import main.model.enums.StudentStatus;
import main.model.grade.Grade;
import main.model.student.HonorsStudent;
import main.model.student.RegularStudent;
import main.model.student.Student;
import main.model.subject.CoreSubject;
import main.model.subject.Subject;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Scanner;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Mocks StudentManager, GradeManager and GPACalculator to verify
 * CalculateGpaAction's branching in isolation - in particular, the four
 * performance-tier messages, the honors-eligibility line (including a
 * HonorsStudent that is eligible vs. one that is not, and a plain
 * RegularStudent that never shows the line at all), and the
 * above/below-class-average comparison - plus the short-circuit on an
 * unknown student ID that the real seeded main.repository can never actually
 * produce (findStudent() there returns null instead of throwing, but
 * CalculateGpaAction itself is what turns that null into the exception).
 */
class CalculateGpaActionMockitoTest {

    private static final String STUDENT_ID = "STU001";
    private final Subject subject = new CoreSubject("Mathematics", "MATH01");

    private String runWithInput(StudentManager studentManager, GradeManager gradeManager,
                                 GPACalculator gpaCalculator, String scriptedInput) {
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

    private Student regularStudent() {
        return new RegularStudent(STUDENT_ID, "Musa Nkusi", 17, "musa@school.edu",
                "1234567890", StudentStatus.ACTIVE);
    }

    @Test
    @DisplayName("execute() throws StudentNotFoundException and never queries grades or GPA when the student isn't found")
    void studentNotFoundThrowsAndShortCircuitsTest() {
        StudentManager studentManager = mock(StudentManager.class);
        GradeManager gradeManager = mock(GradeManager.class);
        GPACalculator gpaCalculator = mock(GPACalculator.class);
        when(studentManager.findStudent("NOPE")).thenReturn(null);
        Scanner scanner = new Scanner(new ByteArrayInputStream("NOPE\n".getBytes(StandardCharsets.UTF_8)));
        CalculateGpaAction action = new CalculateGpaAction(scanner, studentManager, gradeManager, gpaCalculator);

        assertThrows(StudentNotFoundException.class, action::execute);

        verify(gradeManager, never()).getGradesForStudent(any());
        verify(gpaCalculator, never()).cumulativeGPA(any());
        verify(gpaCalculator, never()).classRank(any());
    }

    @Test
    @DisplayName("cumulativeGPA >= 3.5 prints the excellent-performance message")
    void excellentPerformanceBranchTest() {
        StudentManager studentManager = mock(StudentManager.class);
        GradeManager gradeManager = mock(GradeManager.class);
        GPACalculator gpaCalculator = mock(GPACalculator.class);
        Student student = regularStudent();
        when(studentManager.findStudent(STUDENT_ID)).thenReturn(student);
        when(gradeManager.getGradesForStudent(STUDENT_ID)).thenReturn(List.of());
        when(gpaCalculator.cumulativeGPA(STUDENT_ID)).thenReturn(3.6);
        when(studentManager.getAllStudents()).thenReturn(List.of(student));
        when(gpaCalculator.classRank(STUDENT_ID)).thenReturn(1);

        String output = runWithInput(studentManager, gradeManager, gpaCalculator, STUDENT_ID + "\n\n");

        assertTrue(output.contains("Excellent performance (3.5+ GPA)"));
    }

    @Test
    @DisplayName("3.0 <= cumulativeGPA < 3.5 prints the good-performance message")
    void goodPerformanceBranchTest() {
        StudentManager studentManager = mock(StudentManager.class);
        GradeManager gradeManager = mock(GradeManager.class);
        GPACalculator gpaCalculator = mock(GPACalculator.class);
        Student student = regularStudent();
        when(studentManager.findStudent(STUDENT_ID)).thenReturn(student);
        when(gradeManager.getGradesForStudent(STUDENT_ID)).thenReturn(List.of());
        when(gpaCalculator.cumulativeGPA(STUDENT_ID)).thenReturn(3.2);
        when(studentManager.getAllStudents()).thenReturn(List.of(student));
        when(gpaCalculator.classRank(STUDENT_ID)).thenReturn(1);

        String output = runWithInput(studentManager, gradeManager, gpaCalculator, STUDENT_ID + "\n\n");

        assertTrue(output.contains("Good performance (3.0+ GPA)"));
    }

    @Test
    @DisplayName("2.0 <= cumulativeGPA < 3.0 prints the satisfactory-performance message")
    void satisfactoryPerformanceBranchTest() {
        StudentManager studentManager = mock(StudentManager.class);
        GradeManager gradeManager = mock(GradeManager.class);
        GPACalculator gpaCalculator = mock(GPACalculator.class);
        Student student = regularStudent();
        when(studentManager.findStudent(STUDENT_ID)).thenReturn(student);
        when(gradeManager.getGradesForStudent(STUDENT_ID)).thenReturn(List.of());
        when(gpaCalculator.cumulativeGPA(STUDENT_ID)).thenReturn(2.5);
        when(studentManager.getAllStudents()).thenReturn(List.of(student));
        when(gpaCalculator.classRank(STUDENT_ID)).thenReturn(1);

        String output = runWithInput(studentManager, gradeManager, gpaCalculator, STUDENT_ID + "\n\n");

        assertTrue(output.contains("Satisfactory performance (2.0+ GPA)"));
    }

    @Test
    @DisplayName("cumulativeGPA < 2.0 prints the needs-improvement message")
    void needsImprovementBranchTest() {
        StudentManager studentManager = mock(StudentManager.class);
        GradeManager gradeManager = mock(GradeManager.class);
        GPACalculator gpaCalculator = mock(GPACalculator.class);
        Student student = regularStudent();
        when(studentManager.findStudent(STUDENT_ID)).thenReturn(student);
        when(gradeManager.getGradesForStudent(STUDENT_ID)).thenReturn(List.of());
        when(gpaCalculator.cumulativeGPA(STUDENT_ID)).thenReturn(1.0);
        when(studentManager.getAllStudents()).thenReturn(List.of(student));
        when(gpaCalculator.classRank(STUDENT_ID)).thenReturn(1);

        String output = runWithInput(studentManager, gradeManager, gpaCalculator, STUDENT_ID + "\n\n");

        assertTrue(output.contains("Needs improvement (below 2.0 GPA)"));
    }

    @Test
    @DisplayName("An eligible HonorsStudent shows the honors-eligibility-maintained line")
    void honorsEligibleShowsLineTest() {
        StudentManager studentManager = mock(StudentManager.class);
        GradeManager gradeManager = mock(GradeManager.class);
        GPACalculator gpaCalculator = mock(GPACalculator.class);
        HonorsStudent honorsStudent = mock(HonorsStudent.class);
        when(honorsStudent.checkHonorsEligibility()).thenReturn(true);
        when(studentManager.findStudent(STUDENT_ID)).thenReturn(honorsStudent);
        when(gradeManager.getGradesForStudent(STUDENT_ID)).thenReturn(List.of());
        when(gpaCalculator.cumulativeGPA(STUDENT_ID)).thenReturn(3.0);
        when(studentManager.getAllStudents()).thenReturn(List.of(honorsStudent));
        when(gpaCalculator.classRank(STUDENT_ID)).thenReturn(1);

        String output = runWithInput(studentManager, gradeManager, gpaCalculator, STUDENT_ID + "\n\n");

        assertTrue(output.contains("Honors eligibility maintained"));
    }

    @Test
    @DisplayName("An ineligible HonorsStudent does NOT show the honors-eligibility-maintained line")
    void honorsIneligibleHidesLineTest() {
        StudentManager studentManager = mock(StudentManager.class);
        GradeManager gradeManager = mock(GradeManager.class);
        GPACalculator gpaCalculator = mock(GPACalculator.class);
        HonorsStudent honorsStudent = mock(HonorsStudent.class);
        when(honorsStudent.checkHonorsEligibility()).thenReturn(false);
        when(studentManager.findStudent(STUDENT_ID)).thenReturn(honorsStudent);
        when(gradeManager.getGradesForStudent(STUDENT_ID)).thenReturn(List.of());
        when(gpaCalculator.cumulativeGPA(STUDENT_ID)).thenReturn(3.0);
        when(studentManager.getAllStudents()).thenReturn(List.of(honorsStudent));
        when(gpaCalculator.classRank(STUDENT_ID)).thenReturn(1);

        String output = runWithInput(studentManager, gradeManager, gpaCalculator, STUDENT_ID + "\n\n");

        assertFalse(output.contains("Honors eligibility maintained"));
    }

    @Test
    @DisplayName("A RegularStudent never shows the honors-eligibility-maintained line")
    void regularStudentHidesLineTest() {
        StudentManager studentManager = mock(StudentManager.class);
        GradeManager gradeManager = mock(GradeManager.class);
        GPACalculator gpaCalculator = mock(GPACalculator.class);
        Student student = regularStudent();
        when(studentManager.findStudent(STUDENT_ID)).thenReturn(student);
        when(gradeManager.getGradesForStudent(STUDENT_ID)).thenReturn(List.of());
        when(gpaCalculator.cumulativeGPA(STUDENT_ID)).thenReturn(3.0);
        when(studentManager.getAllStudents()).thenReturn(List.of(student));
        when(gpaCalculator.classRank(STUDENT_ID)).thenReturn(1);

        String output = runWithInput(studentManager, gradeManager, gpaCalculator, STUDENT_ID + "\n\n");

        assertFalse(output.contains("Honors eligibility maintained"));
    }

    @Test
    @DisplayName("cumulativeGPA above the class average GPA prints the above-class-average message")
    void aboveClassAverageBranchTest() {
        StudentManager studentManager = mock(StudentManager.class);
        GradeManager gradeManager = mock(GradeManager.class);
        GPACalculator gpaCalculator = mock(GPACalculator.class);
        Student student = regularStudent(); // calculateAverageGrade() == 0.0 (no grades added)
        when(studentManager.findStudent(STUDENT_ID)).thenReturn(student);
        when(gradeManager.getGradesForStudent(STUDENT_ID)).thenReturn(List.of());
        when(gpaCalculator.cumulativeGPA(STUDENT_ID)).thenReturn(3.0);
        when(studentManager.getAllStudents()).thenReturn(List.of(student));
        when(gpaCalculator.classRank(STUDENT_ID)).thenReturn(1);
        when(gpaCalculator.percentageToGPA(0.0)).thenReturn(2.0); // classAvgGPA

        String output = runWithInput(studentManager, gradeManager, gpaCalculator, STUDENT_ID + "\n\n");

        assertTrue(output.contains("Above class average (2.00 GPA)"));
    }

    @Test
    @DisplayName("cumulativeGPA at or below the class average GPA prints the below-class-average message")
    void belowClassAverageBranchTest() {
        StudentManager studentManager = mock(StudentManager.class);
        GradeManager gradeManager = mock(GradeManager.class);
        GPACalculator gpaCalculator = mock(GPACalculator.class);
        Student student = regularStudent(); // calculateAverageGrade() == 0.0 (no grades added)
        when(studentManager.findStudent(STUDENT_ID)).thenReturn(student);
        when(gradeManager.getGradesForStudent(STUDENT_ID)).thenReturn(List.of());
        when(gpaCalculator.cumulativeGPA(STUDENT_ID)).thenReturn(1.0);
        when(studentManager.getAllStudents()).thenReturn(List.of(student));
        when(gpaCalculator.classRank(STUDENT_ID)).thenReturn(1);
        when(gpaCalculator.percentageToGPA(0.0)).thenReturn(2.0); // classAvgGPA

        String output = runWithInput(studentManager, gradeManager, gpaCalculator, STUDENT_ID + "\n\n");

        assertTrue(output.contains("Below class average (2.00 GPA)"));
    }

    @Test
    @DisplayName("Each recorded grade is rendered with its own percentage-to-GPA conversion and letter")
    void gradeLinesRenderEachRecordedGradeTest() {
        StudentManager studentManager = mock(StudentManager.class);
        GradeManager gradeManager = mock(GradeManager.class);
        GPACalculator gpaCalculator = mock(GPACalculator.class);
        Student student = regularStudent();
        Grade grade = new Grade(STUDENT_ID, subject, 85.0);
        when(studentManager.findStudent(STUDENT_ID)).thenReturn(student);
        when(gradeManager.getGradesForStudent(STUDENT_ID)).thenReturn(List.of(grade));
        when(gpaCalculator.percentageToGPA(85.0)).thenReturn(3.0);
        when(gpaCalculator.gpaToLetter(3.0)).thenReturn("B");
        when(gpaCalculator.cumulativeGPA(STUDENT_ID)).thenReturn(3.0);
        when(studentManager.getAllStudents()).thenReturn(List.of(student));
        when(gpaCalculator.classRank(STUDENT_ID)).thenReturn(1);

        String output = runWithInput(studentManager, gradeManager, gpaCalculator, STUDENT_ID + "\n\n");

        assertTrue(output.contains("Mathematics"));
        assertTrue(output.contains("3.0 (B)"));
        verify(gpaCalculator, times(1)).percentageToGPA(85.0);
    }

    @Test
    @DisplayName("getOptionNumber() and getLabel() report the expected menu metadata")
    void menuMetadataTest() {
        StudentManager studentManager = mock(StudentManager.class);
        GradeManager gradeManager = mock(GradeManager.class);
        GPACalculator gpaCalculator = mock(GPACalculator.class);
        CalculateGpaAction action = new CalculateGpaAction(new Scanner(new ByteArrayInputStream(new byte[0])),
                studentManager, gradeManager, gpaCalculator);

        assertEquals(6, action.getOptionNumber());
        assertEquals("Calculate Student GPA", action.getLabel());
    }
}
