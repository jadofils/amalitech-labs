package tests.console;

import console.BulkImportAction;
import imports.BulkImportService;
import manager.GradeManager;
import manager.StudentManager;
import model.enums.Role;
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
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Wires the real BulkImportService/StudentManager/GradeManager stack (the
 * same wiring Main.java uses) to verify BulkImportAction's happy path and
 * validation branches end-to-end. BulkImportActionMockitoTest verifies the
 * same behavior purely through a mocked BulkImportService, including
 * branches (e.g. a non-zero fail count, an ImportException) that are
 * awkward to construct through the real CSV/service stack.
 */
class BulkImportActionTest {

    private static final Pattern LOG_FILENAME_PATTERN = Pattern.compile("See (\\S+) for details");

    private StudentRepositoryImpl studentRepository;
    private SubjectRepositoryImpl subjectRepository;
    private GradeManager gradeManager;
    private StudentManager studentManager;
    private BulkImportService bulkImportService;

    private String csvFilename;
    private String logFilename;

    @BeforeEach
    void setUp() {
        studentRepository = new StudentRepositoryImpl();
        subjectRepository = new SubjectRepositoryImpl();
        GradeService gradeService = new GradeServiceImpl(studentRepository, subjectRepository);
        gradeManager = new GradeManager(gradeService, subjectRepository);
        StudentService studentService = new StudentServiceImpl(studentRepository);
        studentManager = new StudentManager(studentService, gradeManager);
        bulkImportService = new BulkImportService(subjectRepository, studentManager, gradeManager);
    }

    private void writeCsv(String filename, String content) throws IOException {
        // imports/ is only tracked via .gitkeep (git doesn't track empty
        // directories), so a fresh checkout - e.g. CI - won't have it yet.
        new java.io.File("imports").mkdirs();
        try (FileWriter writer = new FileWriter("imports/" + filename + ".csv")) {
            writer.write(content);
        }
    }

    private String extractLogFilename(String output) {
        Matcher matcher = LOG_FILENAME_PATTERN.matcher(output);
        return matcher.find() ? matcher.group(1) : null;
    }

    @AfterEach
    void cleanUp() throws IOException {
        if (csvFilename != null) {
            Files.deleteIfExists(Path.of("imports/" + csvFilename + ".csv"));
        }
        if (logFilename != null) {
            Files.deleteIfExists(Path.of("imports/" + logFilename));
        }
    }

    private String runWithInput(String scriptedInput) {
        Scanner scanner = new Scanner(new ByteArrayInputStream(scriptedInput.getBytes(StandardCharsets.UTF_8)));
        PrintStream originalOut = System.out;
        ByteArrayOutputStream captured = new ByteArrayOutputStream();
        try {
            System.setOut(new PrintStream(captured, true, StandardCharsets.UTF_8));
            new BulkImportAction(scanner, bulkImportService).execute();
        } finally {
            System.setOut(originalOut);
        }
        return captured.toString(StandardCharsets.UTF_8);
    }

    @Test
    @DisplayName("Happy path: a CSV with one valid row for a seeded student imports successfully")
    void validCsvImportsSuccessfullyTest() throws IOException {
        Student student = studentRepository.getAllStudents().get(0);
        csvFilename = "action-test-" + System.nanoTime();
        writeCsv(csvFilename, "StudentID,SubjectName,SubjectType,Grade\n"
                + student.getStudentId() + ",Mathematics,Core,85\n");

        String output = runWithInput(csvFilename + "\n\n");
        logFilename = extractLogFilename(output);

        assertTrue(output.contains("IMPORT SUMMARY"));
        assertTrue(output.contains("Successfully Imported: 1"));
        assertTrue(output.contains("Import completed!"));
        assertEquals(1, gradeManager.getGradeCount());
    }

    @Test
    @DisplayName("A row with a subject-type mismatch is skipped and reported under Failed Records")
    void csvWithInvalidRowReportsFailedRecordsTest() throws IOException {
        Student student = studentRepository.getAllStudents().get(0);
        csvFilename = "action-test-fail-" + System.nanoTime();
        // Mathematics is seeded as Core, so declaring it Elective is a
        // subject-type mismatch per CSVParser - a valid-looking row that
        // still fails without needing an unknown student ID.
        writeCsv(csvFilename, "StudentID,SubjectName,SubjectType,Grade\n"
                + student.getStudentId() + ",Mathematics,Core,85\n"
                + student.getStudentId() + ",Mathematics,Elective,90\n");

        String output = runWithInput(csvFilename + "\n\n");
        logFilename = extractLogFilename(output);

        assertTrue(output.contains("Successfully Imported: 1"));
        assertTrue(output.contains("Failed: 1"));
        assertTrue(output.contains("Failed Records:"));
        assertTrue(output.contains("Subject type mismatch"));
    }

    @Test
    @DisplayName("Empty filename prints 'Filename cannot be empty.' and never touches the import service")
    void emptyFilenamePrintsMessageTest() {
        String output = runWithInput("\n");

        assertTrue(output.contains("Filename cannot be empty."));
        assertEquals(0, gradeManager.getGradeCount());
    }

    @Test
    @DisplayName("A missing file is caught internally and prints an ERROR line with the file path")
    void missingFileCsvPrintsErrorTest() {
        String missingFilename = "does-not-exist-" + System.nanoTime();

        String output = runWithInput(missingFilename + "\n\n");

        assertTrue(output.contains("ERROR: "));
        assertTrue(output.contains("File: imports/" + missingFilename + ".csv"));
    }

    @Test
    @DisplayName("getOptionNumber(), getLabel() and isAuthorizedFor() report the expected menu metadata")
    void menuMetadataTest() {
        BulkImportAction action = new BulkImportAction(new Scanner(new ByteArrayInputStream(new byte[0])),
                bulkImportService);

        assertEquals(7, action.getOptionNumber());
        assertEquals("Bulk Import Grades", action.getLabel());
        assertTrue(action.isAuthorizedFor(Role.TEACHER));
        assertFalse(action.isAuthorizedFor(Role.STUDENT));
    }
}
