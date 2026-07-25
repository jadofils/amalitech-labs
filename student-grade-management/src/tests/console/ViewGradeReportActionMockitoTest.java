package tests.console;

import main.console.ViewGradeReportAction;
import main.exceptions.StudentNotFoundException;
import main.manager.GradeManager;
import main.manager.StudentManager;
import main.model.enums.StudentStatus;
import main.model.student.RegularStudent;
import main.model.student.Student;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Mocks StudentManager and GradeManager to verify ViewGradeReportAction's
 * control flow in isolation: the delegated gradeManager.viewGradesByStudent()
 * call happens exactly once, with the correct student ID, when the student is
 * found - and never happens when the student lookup fails.
 */
class ViewGradeReportActionMockitoTest {

    @Test
    @DisplayName("execute() delegates to gradeManager.viewGradesByStudent() exactly once with the correct ID")
    void executeDelegatesToGradeManagerTest() {
        StudentManager studentManager = mock(StudentManager.class);
        GradeManager gradeManager = mock(GradeManager.class);
        // Explicit-ID constructor - deliberately not the auto-ID one, since
        // that ID is shared/static across the whole test run and must never
        // be assumed to equal a hardcoded literal (see src/tests/README.md).
        Student student = new RegularStudent("STU001", "Musa Nkusi", 17, "musa@school.edu",
                "1234567890", StudentStatus.ACTIVE);
        when(studentManager.findStudent("STU001")).thenReturn(student);
        Scanner scanner = scannerFor("STU001\n\n");
        ViewGradeReportAction action = new ViewGradeReportAction(scanner, studentManager, gradeManager);

        action.execute();

        verify(gradeManager, times(1)).viewGradesByStudent("STU001");
    }

    @Test
    @DisplayName("execute() throws StudentNotFoundException and never queries grades when the student isn't found")
    void executeUnknownStudentNeverQueriesGradesTest() {
        StudentManager studentManager = mock(StudentManager.class);
        GradeManager gradeManager = mock(GradeManager.class);
        when(studentManager.findStudent("NOPE")).thenReturn(null);
        Scanner scanner = scannerFor("NOPE\n");
        ViewGradeReportAction action = new ViewGradeReportAction(scanner, studentManager, gradeManager);

        assertThrows(StudentNotFoundException.class, action::execute);

        verify(gradeManager, never()).viewGradesByStudent(any());
    }

    private Scanner scannerFor(String input) {
        return new Scanner(new ByteArrayInputStream(input.getBytes(StandardCharsets.UTF_8)));
    }
}
