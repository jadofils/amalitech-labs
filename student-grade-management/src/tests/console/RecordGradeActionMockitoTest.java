package tests.console;

import main.console.RecordGradeAction;
import main.manager.GradeManager;
import main.manager.StudentManager;
import main.model.enums.Role;
import main.model.enums.StudentStatus;
import main.model.enums.SubjectType;
import main.model.grade.Grade;
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

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Mocks StudentManager and GradeManager to verify RecordGradeAction's
 * branching in isolation - in particular, the "no subjects available for
 * this type" short-circuit that the real seeded SubjectRepositoryImpl can
 * never actually produce (it always seeds both Core and Elective subjects),
 * plus interaction verification (addGrade never called on any rejected path).
 */
class RecordGradeActionMockitoTest {

    private final Student student = new RegularStudent("STU001", "Musa Nkusi", 17, "musa@school.edu",
            "1234567890", StudentStatus.ACTIVE);
    private final Subject subject = new CoreSubject("Mathematics", "MATH01");

    private String runWithInput(StudentManager studentManager, GradeManager gradeManager, String scriptedInput) {
        Scanner scanner = new Scanner(new ByteArrayInputStream(scriptedInput.getBytes(StandardCharsets.UTF_8)));
        PrintStream originalOut = System.out;
        ByteArrayOutputStream captured = new ByteArrayOutputStream();
        try {
            System.setOut(new PrintStream(captured, true, StandardCharsets.UTF_8));
            new RecordGradeAction(scanner, studentManager, gradeManager).execute();
        } finally {
            System.setOut(originalOut);
        }
        return captured.toString(StandardCharsets.UTF_8);
    }

    @Test
    @DisplayName("No subjects available for the chosen type prints a message and returns without further prompting")
    void noSubjectsAvailableTest() {
        StudentManager studentManager = mock(StudentManager.class);
        GradeManager gradeManager = mock(GradeManager.class);
        when(studentManager.findStudent("STU001")).thenReturn(student);
        when(gradeManager.getSubjectsByType(SubjectType.CORE)).thenReturn(List.of());

        String output = runWithInput(studentManager, gradeManager, "STU001\n1\n");

        assertTrue(output.contains("No subjects available for this type."));
        verify(gradeManager, never()).addGrade(any());
    }

    @Test
    @DisplayName("Invalid (non-numeric) subject selection prints 'Invalid selection.' and never records a grade")
    void invalidSubjectSelectionNonNumericTest() {
        StudentManager studentManager = mock(StudentManager.class);
        GradeManager gradeManager = mock(GradeManager.class);
        when(studentManager.findStudent("STU001")).thenReturn(student);
        when(gradeManager.getSubjectsByType(SubjectType.CORE)).thenReturn(List.of(subject));

        String output = runWithInput(studentManager, gradeManager, "STU001\n1\nabc\n");

        assertTrue(output.contains("Invalid selection."));
        verify(gradeManager, never()).addGrade(any());
    }

    @Test
    @DisplayName("Out-of-range subject selection number prints 'Invalid selection.'")
    void outOfRangeSubjectSelectionTest() {
        StudentManager studentManager = mock(StudentManager.class);
        GradeManager gradeManager = mock(GradeManager.class);
        when(studentManager.findStudent("STU001")).thenReturn(student);
        when(gradeManager.getSubjectsByType(SubjectType.CORE)).thenReturn(List.of(subject));

        String output = runWithInput(studentManager, gradeManager, "STU001\n1\n99\n");

        assertTrue(output.contains("Invalid selection."));
        verify(gradeManager, never()).addGrade(any());
    }

    @Test
    @DisplayName("Invalid (non-numeric) grade input prints 'Invalid grade.' and never records a grade")
    void invalidGradeInputTest() {
        StudentManager studentManager = mock(StudentManager.class);
        GradeManager gradeManager = mock(GradeManager.class);
        when(studentManager.findStudent("STU001")).thenReturn(student);
        when(gradeManager.getSubjectsByType(SubjectType.CORE)).thenReturn(List.of(subject));

        String output = runWithInput(studentManager, gradeManager, "STU001\n1\n1\nabc\n");

        assertTrue(output.contains("Invalid grade."));
        verify(gradeManager, never()).addGrade(any());
    }

    @Test
    @DisplayName("Declining the confirmation prints 'Grade recording cancelled.' and never records a grade")
    void declinedConfirmationCancelsTest() {
        StudentManager studentManager = mock(StudentManager.class);
        GradeManager gradeManager = mock(GradeManager.class);
        when(studentManager.findStudent("STU001")).thenReturn(student);
        when(gradeManager.getSubjectsByType(SubjectType.CORE)).thenReturn(List.of(subject));

        String output = runWithInput(studentManager, gradeManager, "STU001\n1\n1\n85\nN\n");

        assertTrue(output.contains("Grade recording cancelled."));
        verify(gradeManager, never()).addGrade(any());
    }

    @Test
    @DisplayName("Confirming with Y records exactly one grade for the chosen subject and value")
    void confirmedGradeIsRecordedExactlyOnceTest() {
        StudentManager studentManager = mock(StudentManager.class);
        GradeManager gradeManager = mock(GradeManager.class);
        when(studentManager.findStudent("STU001")).thenReturn(student);
        when(gradeManager.getSubjectsByType(SubjectType.CORE)).thenReturn(List.of(subject));

        String output = runWithInput(studentManager, gradeManager, "STU001\n1\n1\n85\nY\n\n");

        assertTrue(output.contains("GRADE CONFIRMATION"));
        assertTrue(output.contains("Grade recorded successfully"));
        org.mockito.ArgumentCaptor<Grade> captor = org.mockito.ArgumentCaptor.forClass(Grade.class);
        verify(gradeManager, times(1)).addGrade(captor.capture());
        Grade captured = captor.getValue();
        assertEquals("STU001", captured.getStudentId());
        assertEquals(85.0, captured.getGrade(), 0.0001);
        assertEquals(subject, captured.getSubject());
    }

    @Test
    @DisplayName("getOptionNumber(), getLabel() and isAuthorizedFor() report the expected menu metadata")
    void menuMetadataTest() {
        StudentManager studentManager = mock(StudentManager.class);
        GradeManager gradeManager = mock(GradeManager.class);
        RecordGradeAction action = new RecordGradeAction(new Scanner(new ByteArrayInputStream(new byte[0])),
                studentManager, gradeManager);

        assertEquals(3, action.getOptionNumber());
        assertEquals("Record Grade", action.getLabel());
        assertTrue(action.isAuthorizedFor(Role.TEACHER));
        assertFalse(action.isAuthorizedFor(Role.STUDENT));
    }
}
