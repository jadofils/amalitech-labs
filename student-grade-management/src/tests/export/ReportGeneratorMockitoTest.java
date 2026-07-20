package tests.export;

import export.ReportGenerator;
import manager.GradeManager;
import manager.StudentManager;
import model.student.RegularStudent;
import model.student.Student;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Mocks GradeManager and StudentManager to verify ReportGenerator reads the
 * student's name and grade data through them, rather than through some
 * other path.
 */
class ReportGeneratorMockitoTest {

    @Test
    @DisplayName("exportSummary() reads the name via StudentManager and the average via GradeManager")
    void exportSummaryDelegatesTest() {
        GradeManager gradeManager = mock(GradeManager.class);
        StudentManager studentManager = mock(StudentManager.class);
        ReportGenerator generator = new ReportGenerator(gradeManager, studentManager);
        Student student = new RegularStudent("STU001", "Musa Nkusi", 17, "musa@school.edu",
                "1234567890", model.enums.StudentStatus.ACTIVE);
        when(studentManager.findStudent("STU001")).thenReturn(student);
        when(gradeManager.calculateOverallAverage("STU001")).thenReturn(72.5);

        String summary = generator.exportSummary("STU001");

        assertTrue(summary.contains("Musa Nkusi"));
        assertTrue(summary.contains("72.5%"));
        verify(studentManager, times(1)).findStudent("STU001");
        verify(gradeManager, times(1)).calculateOverallAverage("STU001");
    }

    @Test
    @DisplayName("exportDetailed() pulls grades, core average, and elective average from GradeManager")
    void exportDetailedDelegatesTest() {
        GradeManager gradeManager = mock(GradeManager.class);
        StudentManager studentManager = mock(StudentManager.class);
        ReportGenerator generator = new ReportGenerator(gradeManager, studentManager);
        when(gradeManager.getGradesForStudent("STU001")).thenReturn(List.of());
        when(gradeManager.calculateCoreAverage("STU001")).thenReturn(0.0);
        when(gradeManager.calculateElectiveAverage("STU001")).thenReturn(0.0);
        when(gradeManager.calculateOverallAverage("STU001")).thenReturn(0.0);

        generator.exportDetailed("STU001");

        verify(gradeManager, times(1)).getGradesForStudent("STU001");
        verify(gradeManager, times(1)).calculateCoreAverage("STU001");
        verify(gradeManager, times(1)).calculateElectiveAverage("STU001");
    }
}
