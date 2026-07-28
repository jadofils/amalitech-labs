package tests.console;

import main.console.ExportClassReportAction;
import main.export.FileExporter;
import main.export.ReportGenerator;
import main.manager.GradeManager;
import main.manager.StudentManager;
import main.model.grade.Grade;
import main.model.student.Student;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import main.repository.student.StudentRepositoryImpl;
import main.repository.subject.SubjectRepositoryImpl;
import main.service.GradeService;
import main.service.GradeServiceImpl;
import main.service.StudentService;
import main.service.StudentServiceImpl;

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
 * Wires the real StudentServiceImpl/GradeManager/SubjectRepositoryImpl stack
 * (the same wiring Main.java uses) plus a real ReportGenerator, to verify
 * ExportClassReportAction end-to-end. The FileExporter is redirected to a
 * temp directory (same pattern as ExportGradeReportActionTest) so assertions
 * can inspect the actual files written to disk.
 * ExportClassReportActionMockitoTest verifies the same branches through
 * mocked collaborators.
 */
class ExportClassReportActionTest {

    private final String testDir = "target/test-export-class-report-" + System.nanoTime();

    private SubjectRepositoryImpl subjectRepository;
    private StudentManager studentManager;
    private GradeManager gradeManager;
    private ReportGenerator reportGenerator;

    @BeforeEach
    void setUp() {
        StudentRepositoryImpl studentRepository = new StudentRepositoryImpl();
        subjectRepository = new SubjectRepositoryImpl();
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
    @DisplayName("Selecting one class and the Text format writes a single '_report.txt' file")
    void singleClassTextFormatTest() throws IOException {
        Student student = studentManager.getAllStudents().get(0);
        gradeManager.addGrade(new Grade(student.getStudentId(), subjectRepository.findSubjectByCode("MATH01"), 85.0));
        ExportClassReportAction action = actionWithInput("1\n1\nmyclass\n\n");

        String output = captureStdOut(action::execute);

        assertTrue(output.contains("exported successfully"));
        Path reportFile = Path.of(testDir, "myclass_MATH01_report.txt");
        assertTrue(Files.exists(reportFile));
        assertTrue(Files.readString(reportFile).contains("CLASS GRADE REPORT - DETAILED"));
        File[] files = new File(testDir).listFiles();
        assertNotNull(files);
        assertEquals(1, files.length);
    }

    @Test
    @DisplayName("Selecting 'all' classes with CSV format writes one CSV file per configured subject")
    void allClassesCsvFormatTest() {
        int subjectCount = subjectRepository.getAllSubjects().size();
        ExportClassReportAction action = actionWithInput("all\n2\nmyclass\n\n");

        String output = captureStdOut(action::execute);

        assertTrue(output.contains("exported successfully"));
        File[] files = new File(testDir).listFiles();
        assertNotNull(files);
        assertEquals(subjectCount, files.length);
    }

    @Test
    @DisplayName("Selecting two classes by comma-separated number with 'All formats' writes 3 files per class")
    void multipleClassesAllFormatsTest() {
        ExportClassReportAction action = actionWithInput("1,2\n4\nmyclass\n\n");

        String output = captureStdOut(action::execute);

        assertTrue(output.contains("exported successfully"));
        File[] files = new File(testDir).listFiles();
        assertNotNull(files);
        assertEquals(6, files.length, "2 classes x 3 formats (report.txt, csv, json) = 6 files");
        assertTrue(Files.exists(Path.of(testDir, "myclass_MATH01_report.txt")));
        assertTrue(Files.exists(Path.of(testDir, "myclass_MATH01.csv")));
        assertTrue(Files.exists(Path.of(testDir, "myclass_MATH01.json")));
        assertTrue(Files.exists(Path.of(testDir, "myclass_ENGL01_report.txt")));
        assertTrue(Files.exists(Path.of(testDir, "myclass_ENGL01.csv")));
        assertTrue(Files.exists(Path.of(testDir, "myclass_ENGL01.json")));
    }

    @Test
    @DisplayName("An out-of-range class number prints 'No valid class selected.' and creates no files")
    void invalidClassSelectionTest() {
        ExportClassReportAction action = actionWithInput("99\n\n");

        String output = captureStdOut(action::execute);

        assertTrue(output.contains("No valid class selected."));
        assertFalse(new File(testDir).exists());
    }

    @Test
    @DisplayName("Empty filename prints a message and creates no files or directory")
    void emptyFilenameTest() {
        ExportClassReportAction action = actionWithInput("1\n1\n\n");

        String output = captureStdOut(action::execute);

        assertTrue(output.contains("Filename cannot be empty."));
        assertFalse(new File(testDir).exists());
    }

    @Test
    @DisplayName("getOptionNumber() and getLabel() identify this as menu option 12")
    void menuMetadataTest() {
        ExportClassReportAction action = actionWithInput("");

        assertEquals(12, action.getOptionNumber());
        assertEquals("Export Class Report", action.getLabel());
    }

    private ExportClassReportAction actionWithInput(String scriptedInput) {
        Scanner scanner = new Scanner(new ByteArrayInputStream(scriptedInput.getBytes(StandardCharsets.UTF_8)));
        FileExporter fileExporter = new FileExporter(testDir);
        return new ExportClassReportAction(scanner, subjectRepository, gradeManager, reportGenerator, fileExporter);
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
