package tests.export;

import main.export.ReportGenerator;
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
                "1234567890", main.model.enums.StudentStatus.ACTIVE);
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

    @Test
    @DisplayName("exportClassDetailed() filters GradeManager.getAllGrades() down to the requested subject and resolves each student's name")
    void exportClassDetailedFiltersBySubjectAndResolvesNamesTest() {
        GradeManager gradeManager = mock(GradeManager.class);
        StudentManager studentManager = mock(StudentManager.class);
        ReportGenerator generator = new ReportGenerator(gradeManager, studentManager);
        Subject mathematics = new CoreSubject("Mathematics", "MATH01");
        Subject english = new CoreSubject("English", "ENGL01");
        Student student = new RegularStudent("STU001", "Musa Nkusi", 17, "musa@school.edu",
                "1234567890", main.model.enums.StudentStatus.ACTIVE);
        when(studentManager.findStudent("STU001")).thenReturn(student);
        when(gradeManager.getAllGrades()).thenReturn(List.of(
                new Grade("STU001", mathematics, 85.0),
                new Grade("STU001", english, 70.0)
        ));

        String content = generator.exportClassDetailed(mathematics);

        assertTrue(content.contains("Total Grades: 1"));
        assertTrue(content.contains("Musa Nkusi"));
        assertFalse(content.contains("70.0%"));
        verify(gradeManager, times(1)).getAllGrades();
    }
}
