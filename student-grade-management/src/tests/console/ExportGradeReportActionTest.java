package tests.console;

import console.ExportGradeReportAction;
import exceptions.StudentNotFoundException;
import export.FileExporter;
import export.ReportGenerator;
import manager.GradeManager;
import manager.StudentManager;
import model.student.Student;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import repository.student.StudentRepositoryImpl;
import repository.subject.SubjectRepositoryImpl;
import service.GradeService;
import service.GradeServiceImpl;
import service.StudentService;
import service.StudentServiceImpl;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Scanner;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Wires the real StudentServiceImpl/GradeManager stack (the same wiring
 * Main.java uses, mirroring StudentManagerTest's/ViewGradeReportActionTest's
 * @BeforeEach) plus a real ReportGenerator, to verify ExportGradeReportAction
 * end-to-end. The FileExporter is redirected to a temp directory (the same
 * pattern as FileExporterTest) instead of the real reports/ folder, so
 * assertions can inspect the actual files written to disk.
 * ExportGradeReportActionMockitoTest verifies the same branches through
 * mocked collaborators.
 */
class ExportGradeReportActionTest {

    private final String testDir = "target/test-export-grade-report-" + System.nanoTime();

    private StudentManager studentManager;
    private GradeManager gradeManager;
    private ReportGenerator reportGenerator;

    @BeforeEach
    void setUp() {
        StudentRepositoryImpl studentRepository = new StudentRepositoryImpl();
        SubjectRepositoryImpl subjectRepository = new SubjectRepositoryImpl();
        GradeService gradeService = new GradeServiceImpl(studentRepository, subjectRepository);
        gradeManager = new GradeManager(gradeService, subjectRepository);
        StudentService studentService = new StudentServiceImpl(studentRepository);
        studentManager = new StudentManager(studentService, gradeManager);
        reportGenerator = new ReportGenerator(gradeManager, studentManager);
    }

    @AfterEach
    void cleanUp() throws IOException {
        File dir = new File(testDir);
        File[] files = dir.listFiles();
        if (files != null) {
            for (File f : files) {
                Files.deleteIfExists(f.toPath());
            }
        }
        Files.deleteIfExists(Path.of(testDir));
    }

    @Test
    @DisplayName("execute() throws StudentNotFoundException for an unknown student ID")
    void studentNotFoundTest() {
        ExportGradeReportAction action = actionWithInput("NOPE\n");

        assertThrows(StudentNotFoundException.class, action::execute);
    }

    @Test
    @DisplayName("Option 1 (summary only) writes a single '_summary.txt' file and reports success")
    void exportSummaryOnlyTest() throws IOException {
        Student student = studentManager.getAllStudents().get(0);
        // Trailing blank line is the "Press Enter to continue..." keystroke read by ConsoleUtils.promptEnter().
        ExportGradeReportAction action = actionWithInput(student.getStudentId() + "\n1\nmyreport\n\n");

        String output = captureStdOut(action::execute);

        assertTrue(output.contains("Report exported successfully!"));
        File[] files = new File(testDir).listFiles();
        assertNotNull(files);
        assertEquals(1, files.length, "only the summary file should be written");
        Path summaryFile = Path.of(testDir, "myreport_summary.txt");
        assertTrue(Files.exists(summaryFile));
        assertTrue(Files.readString(summaryFile).contains("STUDENT GRADE REPORT - SUMMARY"));
    }

    @Test
    @DisplayName("Option 2 (detailed only) writes a single '_detailed.txt' file and reports success")
    void exportDetailedOnlyTest() throws IOException {
        Student student = studentManager.getAllStudents().get(0);
        ExportGradeReportAction action = actionWithInput(student.getStudentId() + "\n2\nmyreport\n\n");

        String output = captureStdOut(action::execute);

        assertTrue(output.contains("Report exported successfully!"));
        File[] files = new File(testDir).listFiles();
        assertNotNull(files);
        assertEquals(1, files.length, "only the detailed file should be written");
        Path detailedFile = Path.of(testDir, "myreport_detailed.txt");
        assertTrue(Files.exists(detailedFile));
        assertTrue(Files.readString(detailedFile).contains("STUDENT GRADE REPORT - DETAILED"));
    }

    @Test
    @DisplayName("Option 3 (both) writes both a '_summary.txt' and a '_detailed.txt' file")
    void exportBothTest() throws IOException {
        Student student = studentManager.getAllStudents().get(0);
        ExportGradeReportAction action = actionWithInput(student.getStudentId() + "\n3\nmyreport\n\n");

        String output = captureStdOut(action::execute);

        assertTrue(output.contains("Report exported successfully!"));
        File[] files = new File(testDir).listFiles();
        assertNotNull(files);
        assertEquals(2, files.length, "both files should be written");
        assertTrue(Files.exists(Path.of(testDir, "myreport_summary.txt")));
        assertTrue(Files.exists(Path.of(testDir, "myreport_detailed.txt")));
        assertTrue(output.contains("File: myreport_summary.txt"));
        assertTrue(output.contains("File: myreport_detailed.txt"));
    }

    @Test
    @DisplayName("Empty filename prints a message and aborts before any file or directory is created")
    void emptyFilenameTest() {
        Student student = studentManager.getAllStudents().get(0);
        ExportGradeReportAction action = actionWithInput(student.getStudentId() + "\n1\n\n");

        String output = captureStdOut(action::execute);

        assertTrue(output.contains("Filename cannot be empty."));
        assertFalse(new File(testDir).exists(), "no directory/file should be created when filename is empty");
    }

    @Test
    @DisplayName("getOptionNumber() and getLabel() identify this as menu option 5")
    void menuMetadataTest() {
        ExportGradeReportAction action = actionWithInput("");

        assertEquals(5, action.getOptionNumber());
        assertEquals("Export Grade Report", action.getLabel());
    }

    private ExportGradeReportAction actionWithInput(String scriptedInput) {
        Scanner scanner = new Scanner(new ByteArrayInputStream(scriptedInput.getBytes(StandardCharsets.UTF_8)));
        FileExporter fileExporter = new FileExporter(testDir);
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
