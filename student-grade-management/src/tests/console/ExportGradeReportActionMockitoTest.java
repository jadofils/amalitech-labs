package tests.console;

import main.console.ExportGradeReportAction;
import main.exceptions.StudentNotFoundException;
import main.export.FileExporter;
import main.export.ReportGenerator;
import main.manager.GradeManager;
import main.manager.StudentManager;
import main.model.enums.StudentStatus;
import main.model.student.RegularStudent;
import main.model.student.Student;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Mocks StudentManager, GradeManager, ReportGenerator and FileExporter to
 * verify ExportGradeReportAction's branching in isolation: the student-not-found
 * and empty-filename short-circuits (report/main.export methods must never be
 * called), the summary-only and detailed-only paths delegating to exactly the
 * right ReportGenerator/FileExporter method, and the "both" path invoking
 * summary before detailed in order.
 */
class ExportGradeReportActionMockitoTest {

    private final Student student = new RegularStudent("STU001", "Musa Nkusi", 17, "musa@school.edu",
            "1234567890", StudentStatus.ACTIVE);

    @Test
    @DisplayName("execute() throws StudentNotFoundException and never generates or exports a report when the student isn't found")
    void studentNotFoundNeverExportsTest() {
        StudentManager studentManager = mock(StudentManager.class);
        GradeManager gradeManager = mock(GradeManager.class);
        ReportGenerator reportGenerator = mock(ReportGenerator.class);
        FileExporter fileExporter = mock(FileExporter.class);
        when(studentManager.findStudent("NOPE")).thenReturn(null);
        ExportGradeReportAction action = actionWithInput(studentManager, gradeManager, reportGenerator,
                fileExporter, "NOPE\n");

        assertThrows(StudentNotFoundException.class, action::execute);

        verify(reportGenerator, never()).exportSummary(any());
        verify(reportGenerator, never()).exportDetailed(any());
        verify(fileExporter, never()).exportToFile(any(), any());
    }

    @Test
    @DisplayName("Empty filename prints a message and never generates or exports a report")
    void emptyFilenameNeverExportsTest() {
        StudentManager studentManager = mock(StudentManager.class);
        GradeManager gradeManager = mock(GradeManager.class);
        ReportGenerator reportGenerator = mock(ReportGenerator.class);
        FileExporter fileExporter = mock(FileExporter.class);
        when(studentManager.findStudent("STU001")).thenReturn(student);
        ExportGradeReportAction action = actionWithInput(studentManager, gradeManager, reportGenerator,
                fileExporter, "STU001\n1\n\n");

        String output = captureStdOut(action::execute);

        assertTrue(output.contains("Filename cannot be empty."));
        verify(reportGenerator, never()).exportSummary(any());
        verify(reportGenerator, never()).exportDetailed(any());
        verify(fileExporter, never()).exportToFile(any(), any());
    }

    @Test
    @DisplayName("Option 1 (summary only) exports exactly the summary content to '<filename>_summary.txt' and never touches the detailed path")
    void summaryOnlyDelegatesCorrectlyTest() {
        StudentManager studentManager = mock(StudentManager.class);
        GradeManager gradeManager = mock(GradeManager.class);
        ReportGenerator reportGenerator = mock(ReportGenerator.class);
        FileExporter fileExporter = mock(FileExporter.class);
        when(studentManager.findStudent("STU001")).thenReturn(student);
        when(reportGenerator.exportSummary("STU001")).thenReturn("SUMMARY-CONTENT");
        when(fileExporter.exportToFile("myreport_summary.txt", "SUMMARY-CONTENT"))
                .thenReturn(new FileExporter.FileExportResult("x/myreport_summary.txt", 42L));
        ExportGradeReportAction action = actionWithInput(studentManager, gradeManager, reportGenerator,
                fileExporter, "STU001\n1\nmyreport\n\n");

        String output = captureStdOut(action::execute);

        assertTrue(output.contains("Report exported successfully!"));
        verify(fileExporter, times(1)).exportToFile("myreport_summary.txt", "SUMMARY-CONTENT");
        verify(reportGenerator, never()).exportDetailed(any());
        verify(fileExporter, never()).exportToFile(eq("myreport_detailed.txt"), any());
    }

    @Test
    @DisplayName("Option 2 (detailed only) exports exactly the detailed content to '<filename>_detailed.txt' and never touches the summary path")
    void detailedOnlyDelegatesCorrectlyTest() {
        StudentManager studentManager = mock(StudentManager.class);
        GradeManager gradeManager = mock(GradeManager.class);
        ReportGenerator reportGenerator = mock(ReportGenerator.class);
        FileExporter fileExporter = mock(FileExporter.class);
        when(studentManager.findStudent("STU001")).thenReturn(student);
        when(reportGenerator.exportDetailed("STU001")).thenReturn("DETAILED-CONTENT");
        when(fileExporter.exportToFile("myreport_detailed.txt", "DETAILED-CONTENT"))
                .thenReturn(new FileExporter.FileExportResult("x/myreport_detailed.txt", 99L));
        ExportGradeReportAction action = actionWithInput(studentManager, gradeManager, reportGenerator,
                fileExporter, "STU001\n2\nmyreport\n\n");

        String output = captureStdOut(action::execute);

        assertTrue(output.contains("Report exported successfully!"));
        verify(fileExporter, times(1)).exportToFile("myreport_detailed.txt", "DETAILED-CONTENT");
        verify(reportGenerator, never()).exportSummary(any());
        verify(fileExporter, never()).exportToFile(eq("myreport_summary.txt"), any());
    }

    @Test
    @DisplayName("Option 3 (both) generates and exports the summary report before the detailed report")
    void bothOptionsExportSummaryThenDetailedInOrderTest() {
        StudentManager studentManager = mock(StudentManager.class);
        GradeManager gradeManager = mock(GradeManager.class);
        ReportGenerator reportGenerator = mock(ReportGenerator.class);
        FileExporter fileExporter = mock(FileExporter.class);
        when(studentManager.findStudent("STU001")).thenReturn(student);
        when(reportGenerator.exportSummary("STU001")).thenReturn("SUMMARY-CONTENT");
        when(reportGenerator.exportDetailed("STU001")).thenReturn("DETAILED-CONTENT");
        when(fileExporter.exportToFile("myreport_summary.txt", "SUMMARY-CONTENT"))
                .thenReturn(new FileExporter.FileExportResult("x/myreport_summary.txt", 42L));
        when(fileExporter.exportToFile("myreport_detailed.txt", "DETAILED-CONTENT"))
                .thenReturn(new FileExporter.FileExportResult("x/myreport_detailed.txt", 99L));
        ExportGradeReportAction action = actionWithInput(studentManager, gradeManager, reportGenerator,
                fileExporter, "STU001\n3\nmyreport\n\n");

        String output = captureStdOut(action::execute);

        assertTrue(output.contains("Report exported successfully!"));
        assertTrue(output.contains("File: myreport_summary.txt"));
        assertTrue(output.contains("File: myreport_detailed.txt"));
        InOrder inOrder = inOrder(reportGenerator, fileExporter);
        inOrder.verify(reportGenerator).exportSummary("STU001");
        inOrder.verify(fileExporter).exportToFile("myreport_summary.txt", "SUMMARY-CONTENT");
        inOrder.verify(reportGenerator).exportDetailed("STU001");
        inOrder.verify(fileExporter).exportToFile("myreport_detailed.txt", "DETAILED-CONTENT");
    }

    @Test
    @DisplayName("getOptionNumber() and getLabel() identify this as menu option 5")
    void menuMetadataTest() {
        StudentManager studentManager = mock(StudentManager.class);
        GradeManager gradeManager = mock(GradeManager.class);
        ReportGenerator reportGenerator = mock(ReportGenerator.class);
        FileExporter fileExporter = mock(FileExporter.class);
        ExportGradeReportAction action = actionWithInput(studentManager, gradeManager, reportGenerator,
                fileExporter, "");

        assertEquals(5, action.getOptionNumber());
        assertEquals("Export Grade Report", action.getLabel());
    }

    private ExportGradeReportAction actionWithInput(StudentManager studentManager, GradeManager gradeManager,
                                                     ReportGenerator reportGenerator, FileExporter fileExporter,
                                                     String scriptedInput) {
        Scanner scanner = new Scanner(new ByteArrayInputStream(scriptedInput.getBytes(StandardCharsets.UTF_8)));
        return new ExportGradeReportAction(scanner, studentManager, gradeManager, reportGenerator, fileExporter);
    }

    private String captureStdOut(Runnable action) {
        PrintStream original = System.out;
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        System.setOut(new PrintStream(buffer, true, StandardCharsets.UTF_8));
        try {
            action.run();
        } finally {
            System.setOut(original);
        }
        return buffer.toString(StandardCharsets.UTF_8);
    }
}
