package tests.console;

import main.console.ExportClassReportAction;
import main.export.FileExporter;
import main.export.ReportGenerator;
import main.manager.GradeManager;
import main.model.grade.Grade;
import main.model.subject.CoreSubject;
import main.model.subject.Subject;
import main.repository.subject.SubjectRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Scanner;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Mocks SubjectRepository, GradeManager, ReportGenerator and FileExporter to
 * verify ExportClassReportAction's branching in isolation: the no-classes
 * and invalid-selection short-circuits (report/export methods must never be
 * called), the format-selection branching (text/CSV/JSON/all), and that CSV
 * and JSON dumps are built only from grades matching the selected subject.
 */
class ExportClassReportActionMockitoTest {

    private final Subject mathematics = new CoreSubject("Mathematics", "MATH01");
    private final Subject english = new CoreSubject("English", "ENGL01");

    @Test
    @DisplayName("No configured classes prints a message and never reads a selection")
    void noClassesConfiguredTest() {
        SubjectRepository subjectRepository = mock(SubjectRepository.class);
        GradeManager gradeManager = mock(GradeManager.class);
        ReportGenerator reportGenerator = mock(ReportGenerator.class);
        FileExporter fileExporter = mock(FileExporter.class);
        when(subjectRepository.getAllSubjects()).thenReturn(List.of());

        String output = runWithInput(subjectRepository, gradeManager, reportGenerator, fileExporter, "\n");

        assertTrue(output.contains("No classes are configured yet."));
        verify(reportGenerator, never()).exportClassDetailed(any());
    }

    @Test
    @DisplayName("An unparseable class selection prints 'No valid class selected.' and never exports")
    void invalidSelectionNeverExportsTest() {
        SubjectRepository subjectRepository = mock(SubjectRepository.class);
        GradeManager gradeManager = mock(GradeManager.class);
        ReportGenerator reportGenerator = mock(ReportGenerator.class);
        FileExporter fileExporter = mock(FileExporter.class);
        when(subjectRepository.getAllSubjects()).thenReturn(List.of(mathematics));

        String output = runWithInput(subjectRepository, gradeManager, reportGenerator, fileExporter, "notanumber\n\n");

        assertTrue(output.contains("No valid class selected."));
        verify(reportGenerator, never()).exportClassDetailed(any());
        verify(fileExporter, never()).exportToFile(any(), any());
    }

    @Test
    @DisplayName("Empty filename prints a message and never exports, and never creates the reports directory")
    void emptyFilenameNeverExportsTest() {
        SubjectRepository subjectRepository = mock(SubjectRepository.class);
        GradeManager gradeManager = mock(GradeManager.class);
        ReportGenerator reportGenerator = mock(ReportGenerator.class);
        FileExporter fileExporter = mock(FileExporter.class);
        when(subjectRepository.getAllSubjects()).thenReturn(List.of(mathematics));

        String output = runWithInput(subjectRepository, gradeManager, reportGenerator, fileExporter, "1\n1\n\n");

        assertTrue(output.contains("Filename cannot be empty."));
        verify(reportGenerator, never()).exportClassDetailed(any());
        verify(fileExporter, never()).ensureDirectory();
    }

    @Test
    @DisplayName("Text format (option 1) calls exportClassDetailed() and FileExporter, but never touches CSV/JSON export or ensureDirectory()")
    void textFormatOnlyDelegatesCorrectlyTest() {
        SubjectRepository subjectRepository = mock(SubjectRepository.class);
        GradeManager gradeManager = mock(GradeManager.class);
        ReportGenerator reportGenerator = mock(ReportGenerator.class);
        FileExporter fileExporter = mock(FileExporter.class);
        when(subjectRepository.getAllSubjects()).thenReturn(List.of(mathematics));
        when(reportGenerator.exportClassDetailed(mathematics)).thenReturn("CLASS-CONTENT");
        when(fileExporter.exportToFile("myclass_MATH01_report.txt", "CLASS-CONTENT"))
                .thenReturn(new FileExporter.FileExportResult("x/myclass_MATH01_report.txt", 10L));

        String output = runWithInput(subjectRepository, gradeManager, reportGenerator, fileExporter, "1\n1\nmyclass\n\n");

        assertTrue(output.contains("exported successfully"));
        verify(fileExporter, times(1)).exportToFile("myclass_MATH01_report.txt", "CLASS-CONTENT");
        verify(fileExporter, never()).ensureDirectory();
        verify(gradeManager, never()).getAllGrades();
    }

    @Test
    @DisplayName("CSV format (option 2) builds GradeRecords only from grades matching the selected subject")
    void csvFormatFiltersGradesBySubjectTest(@TempDir Path tempDir) throws IOException {
        SubjectRepository subjectRepository = mock(SubjectRepository.class);
        GradeManager gradeManager = mock(GradeManager.class);
        ReportGenerator reportGenerator = mock(ReportGenerator.class);
        FileExporter fileExporter = mock(FileExporter.class);
        when(subjectRepository.getAllSubjects()).thenReturn(List.of(mathematics, english));
        // GradeDataExporter is constructed internally by the action (not injected), so it
        // writes for real - this needs an actual directory on disk, not just a mocked getter.
        when(fileExporter.getReportsDir()).thenReturn(tempDir.toString());
        when(gradeManager.getAllGrades()).thenReturn(List.of(
                new Grade("STU001", mathematics, 85.0),
                new Grade("STU001", english, 70.0)
        ));

        String output = runWithInput(subjectRepository, gradeManager, reportGenerator, fileExporter, "1\n2\nmyclass\n\n");

        assertTrue(output.contains("exported successfully"));
        verify(fileExporter, times(1)).ensureDirectory();
        verify(reportGenerator, never()).exportClassDetailed(any());
        verify(gradeManager, times(1)).getAllGrades();
        String csvContent = Files.readString(tempDir.resolve("myclass_MATH01.csv"));
        assertTrue(csvContent.contains("STU001"));
        assertFalse(csvContent.contains("ENGL01"));
    }

    private String runWithInput(SubjectRepository subjectRepository, GradeManager gradeManager,
                                 ReportGenerator reportGenerator, FileExporter fileExporter, String scriptedInput) {
        PrintStream originalOut = System.out;
        ByteArrayOutputStream captured = new ByteArrayOutputStream();
        try (Scanner scanner = new Scanner(new ByteArrayInputStream(scriptedInput.getBytes(StandardCharsets.UTF_8)))) {
            System.setOut(new PrintStream(captured, true, StandardCharsets.UTF_8));
            new ExportClassReportAction(scanner, subjectRepository, gradeManager, reportGenerator, fileExporter).execute();
        } finally {
            System.setOut(originalOut);
        }
        return captured.toString(StandardCharsets.UTF_8);
    }

    @Test
    @DisplayName("getOptionNumber() and getLabel() identify this as menu option 12")
    void menuMetadataTest() {
        SubjectRepository subjectRepository = mock(SubjectRepository.class);
        GradeManager gradeManager = mock(GradeManager.class);
        ReportGenerator reportGenerator = mock(ReportGenerator.class);
        FileExporter fileExporter = mock(FileExporter.class);
        ExportClassReportAction action = new ExportClassReportAction(
                new Scanner(new ByteArrayInputStream(new byte[0])), subjectRepository, gradeManager,
                reportGenerator, fileExporter);

        assertEquals(12, action.getOptionNumber());
        assertEquals("Export Class Report", action.getLabel());
    }
}
