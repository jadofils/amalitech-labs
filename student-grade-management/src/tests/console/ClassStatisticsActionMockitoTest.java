package tests.console;

import calculators.StatisticsCalculator;
import console.ClassStatisticsAction;
import manager.GradeManager;
import manager.StudentManager;
import model.enums.StudentStatus;
import model.grade.Grade;
import model.student.HonorsStudent;
import model.student.RegularStudent;
import model.student.Student;
import model.subject.CoreSubject;
import model.subject.ElectiveSubject;
import model.subject.Subject;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import repository.subject.SubjectRepository;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Scanner;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Mocks StudentManager, GradeManager, and SubjectRepository to verify
 * ClassStatisticsAction's control flow in isolation - in particular, that the
 * "no grades yet" short-circuit never touches SubjectRepository, and that
 * each conditional line in the STUDENT TYPE COMPARISON section is printed (or
 * omitted) independently based on whether that student type is represented.
 * StatisticsCalculator has no collaborators of its own (see
 * StatisticsCalculatorTest/StatisticsCalculatorMockitoTest), so a real
 * instance is used here too, same as ClassStatisticsActionTest.
 */
class ClassStatisticsActionMockitoTest {

    private final StatisticsCalculator statisticsCalculator = new StatisticsCalculator();

    @Test
    @DisplayName("No grades recorded: reports the totals, returns early, and never queries SubjectRepository")
    void noGradesRecordedShortCircuitsBeforeSubjectRepositoryTest() {
        StudentManager studentManager = mock(StudentManager.class);
        GradeManager gradeManager = mock(GradeManager.class);
        SubjectRepository subjectRepository = mock(SubjectRepository.class);
        Student alice = new RegularStudent("STU001", "Alice Johnson", 17, "alice@school.edu",
                "1234567890", StudentStatus.ACTIVE);
        Student bob = new HonorsStudent("STU002", "Bob Smith", 18, "bob@school.edu",
                "1234567890", StudentStatus.ACTIVE);
        when(studentManager.getAllStudents()).thenReturn(List.of(alice, bob));
        when(gradeManager.getGradesForStudent(anyString())).thenReturn(List.of());

        String output = runAction(studentManager, gradeManager, subjectRepository);

        assertTrue(output.contains("Total Students: 2"));
        assertTrue(output.contains("Total Grades Recorded: 0"));
        assertTrue(output.contains("No grades recorded yet."));
        assertFalse(output.contains("GRADE DISTRIBUTION"));

        verify(studentManager, times(1)).getAllStudents();
        verify(gradeManager, times(1)).getGradesForStudent("STU001");
        verify(gradeManager, times(1)).getGradesForStudent("STU002");
        verify(subjectRepository, never()).getAllSubjects();
    }

    @Test
    @DisplayName("With grades for both Regular and Honors students, every statistics section prints and both type-comparison lines appear")
    void happyPathAggregatesGradesAndPrintsBothTypeLinesTest() {
        StudentManager studentManager = mock(StudentManager.class);
        GradeManager gradeManager = mock(GradeManager.class);
        SubjectRepository subjectRepository = mock(SubjectRepository.class);

        Student alice = new RegularStudent("STU001", "Alice Johnson", 17, "alice@school.edu",
                "1234567890", StudentStatus.ACTIVE);
        alice.addGrade(90.0);
        Student bob = new HonorsStudent("STU002", "Bob Smith", 18, "bob@school.edu",
                "1234567890", StudentStatus.ACTIVE);
        bob.addGrade(60.0);
        Student carol = new RegularStudent("STU003", "Carol Martinez", 16, "carol@school.edu",
                "1234567890", StudentStatus.ACTIVE);
        carol.addGrade(40.0);
        when(studentManager.getAllStudents()).thenReturn(List.of(alice, bob, carol));

        Subject math = new CoreSubject("Mathematics", "MATH01");
        Subject music = new ElectiveSubject("Music", "MUSC01");
        when(gradeManager.getGradesForStudent("STU001")).thenReturn(List.of(new Grade("STU001", math, 90.0)));
        when(gradeManager.getGradesForStudent("STU002")).thenReturn(List.of(new Grade("STU002", music, 60.0)));
        when(gradeManager.getGradesForStudent("STU003")).thenReturn(List.of(new Grade("STU003", math, 40.0)));
        when(subjectRepository.getAllSubjects()).thenReturn(List.of(math, music));

        String output = runAction(studentManager, gradeManager, subjectRepository);

        assertTrue(output.contains("Total Students: 3"));
        assertTrue(output.contains("Total Grades Recorded: 3"));

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
        assertTrue(output.contains("Mathematics:"));
        assertTrue(output.contains("Music:"));

        assertTrue(output.contains("STUDENT TYPE COMPARISON"));
        assertTrue(output.contains("Regular Students:"));
        assertTrue(output.contains("Honors Students:"));

        verify(subjectRepository, times(1)).getAllSubjects();
    }

    @Test
    @DisplayName("Zero Honors students: the Honors Students line is omitted while Regular Students still prints")
    void omitsHonorsLineWhenHonorsCountIsZeroTest() {
        StudentManager studentManager = mock(StudentManager.class);
        GradeManager gradeManager = mock(GradeManager.class);
        SubjectRepository subjectRepository = mock(SubjectRepository.class);

        Student alice = new RegularStudent("STU001", "Alice Johnson", 17, "alice@school.edu",
                "1234567890", StudentStatus.ACTIVE);
        alice.addGrade(80.0);
        when(studentManager.getAllStudents()).thenReturn(List.of(alice));

        Subject math = new CoreSubject("Mathematics", "MATH01");
        when(gradeManager.getGradesForStudent("STU001")).thenReturn(List.of(new Grade("STU001", math, 80.0)));
        when(subjectRepository.getAllSubjects()).thenReturn(List.of(math));

        String output = runAction(studentManager, gradeManager, subjectRepository);

        assertTrue(output.contains("Regular Students:"));
        assertFalse(output.contains("Honors Students:"));
    }

    @Test
    @DisplayName("Zero Regular students: the Regular Students line is omitted while Honors Students still prints")
    void omitsRegularLineWhenRegularCountIsZeroTest() {
        StudentManager studentManager = mock(StudentManager.class);
        GradeManager gradeManager = mock(GradeManager.class);
        SubjectRepository subjectRepository = mock(SubjectRepository.class);

        Student bob = new HonorsStudent("STU002", "Bob Smith", 18, "bob@school.edu",
                "1234567890", StudentStatus.ACTIVE);
        bob.addGrade(95.0);
        when(studentManager.getAllStudents()).thenReturn(List.of(bob));

        Subject math = new CoreSubject("Mathematics", "MATH01");
        when(gradeManager.getGradesForStudent("STU002")).thenReturn(List.of(new Grade("STU002", math, 95.0)));
        when(subjectRepository.getAllSubjects()).thenReturn(List.of(math));

        String output = runAction(studentManager, gradeManager, subjectRepository);

        assertTrue(output.contains("Honors Students:"));
        assertFalse(output.contains("Regular Students:"));
    }

    @Test
    @DisplayName("getOptionNumber() and getLabel() identify this as menu option 8")
    void menuIdentityTest() {
        StudentManager studentManager = mock(StudentManager.class);
        GradeManager gradeManager = mock(GradeManager.class);
        SubjectRepository subjectRepository = mock(SubjectRepository.class);
        ClassStatisticsAction action = new ClassStatisticsAction(scriptedScanner(), studentManager, gradeManager,
                statisticsCalculator, subjectRepository);

        assertEquals(8, action.getOptionNumber());
        assertEquals("View Class Statistics", action.getLabel());
    }

    private Scanner scriptedScanner() {
        return new Scanner(new ByteArrayInputStream("\n".getBytes(StandardCharsets.UTF_8)));
    }

    private String runAction(StudentManager studentManager, GradeManager gradeManager, SubjectRepository subjectRepository) {
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
