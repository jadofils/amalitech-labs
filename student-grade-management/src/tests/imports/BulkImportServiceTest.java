package tests.imports;

import main.dataio.GradeDataExporter;
import main.dataio.GradeRecord;
import main.exceptions.ImportException;
import main.imports.BulkImportService;
import main.manager.GradeManager;
import main.manager.StudentManager;
import main.model.student.Student;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import main.repository.student.StudentRepositoryImpl;
import main.repository.subject.SubjectRepositoryImpl;
import main.service.GradeService;
import main.service.StudentService;
import main.service.GradeServiceImpl;
import main.service.StudentServiceImpl;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class BulkImportServiceTest {

    private final GradeDataExporter gradeDataExporter = new GradeDataExporter();

    private String csvFilename;
    private String logFilename;
    private String otherFormatFilename;
    private String otherFormatExtension;

    private void writeCsv(String filename, String content) throws IOException {
        // imports/ is only tracked via .gitkeep (git doesn't track empty
        // directories), so a fresh checkout - e.g. CI - won't have it yet.
        new java.io.File("imports").mkdirs();
        try (FileWriter writer = new FileWriter("imports/" + filename + ".csv")) {
            writer.write(content);
        }
    }

    @AfterEach
    void cleanUp() throws IOException {
        if (csvFilename != null) {
            Files.deleteIfExists(Path.of("imports/" + csvFilename + ".csv"));
        }
        if (otherFormatFilename != null) {
            Files.deleteIfExists(Path.of("imports/" + otherFormatFilename + "." + otherFormatExtension));
        }
        if (logFilename != null) {
            Files.deleteIfExists(Path.of("imports/" + logFilename));
        }
    }

    @Test
    @DisplayName("importFromFile() records a grade for each valid row and skips an unknown student")
    void importFromFileImportsValidRowsTest() throws IOException {
        StudentRepositoryImpl students = new StudentRepositoryImpl();
        SubjectRepositoryImpl subjects = new SubjectRepositoryImpl();
        GradeService gradeService = new GradeServiceImpl(students, subjects);
        GradeManager gradeManager = new GradeManager(gradeService, subjects);
        StudentService studentService = new StudentServiceImpl(students);
        StudentManager studentManager = new StudentManager(studentService, gradeManager);
        BulkImportService bulkImportService = new BulkImportService(subjects, studentManager, gradeManager);
        Student student = students.getAllStudents().get(0);

        csvFilename = "test-import-" + System.nanoTime();
        writeCsv(csvFilename, "StudentID,SubjectName,SubjectType,Grade\n"
                + student.getStudentId() + ",Mathematics,Core,85\n"
                + "NOPE999,Mathematics,Core,85\n");

        BulkImportService.ImportResult result = bulkImportService.importFromFile(csvFilename);
        logFilename = result.getLogFilename();

        assertEquals(1, result.getSuccessCount());
        assertEquals(1, result.getFailedCount());
        assertEquals(1, gradeManager.getGradeCount());
        assertTrue(Files.exists(Path.of("imports/" + logFilename)));
    }

    @Test
    @DisplayName("importFromFile() throws ImportException for a file that doesn't exist")
    void importFromFileMissingFileThrowsTest() {
        StudentRepositoryImpl students = new StudentRepositoryImpl();
        SubjectRepositoryImpl subjects = new SubjectRepositoryImpl();
        GradeService gradeService = new GradeServiceImpl(students, subjects);
        GradeManager gradeManager = new GradeManager(gradeService, subjects);
        StudentService studentService = new StudentServiceImpl(students);
        StudentManager studentManager = new StudentManager(studentService, gradeManager);
        BulkImportService bulkImportService = new BulkImportService(subjects, studentManager, gradeManager);

        String missingFilename = "does-not-exist-" + System.nanoTime();
        assertThrows(ImportException.class, () -> bulkImportService.importFromFile(missingFilename));
    }

    @Test
    @DisplayName("The generated log file records the total/success/failed counts")
    void importLogContainsCountsTest() throws IOException {
        StudentRepositoryImpl students = new StudentRepositoryImpl();
        SubjectRepositoryImpl subjects = new SubjectRepositoryImpl();
        GradeService gradeService = new GradeServiceImpl(students, subjects);
        GradeManager gradeManager = new GradeManager(gradeService, subjects);
        StudentService studentService = new StudentServiceImpl(students);
        StudentManager studentManager = new StudentManager(studentService, gradeManager);
        BulkImportService bulkImportService = new BulkImportService(subjects, studentManager, gradeManager);
        Student student = students.getAllStudents().get(0);

        csvFilename = "test-import-log-" + System.nanoTime();
        writeCsv(csvFilename, "StudentID,SubjectName,SubjectType,Grade\n"
                + student.getStudentId() + ",Mathematics,Core,85\n");

        BulkImportService.ImportResult result = bulkImportService.importFromFile(csvFilename);
        logFilename = result.getLogFilename();

        String logContent = Files.readString(Path.of("imports/" + logFilename));
        assertTrue(logContent.contains("Successfully Imported: 1"));
        assertTrue(logContent.contains("Failed: 0"));
    }

    @Test
    @DisplayName("importFromFile() auto-detects a .json file, importing a valid record and skipping an unknown student and an out-of-range grade")
    void importFromFileJsonImportsValidRecordsTest() {
        StudentRepositoryImpl students = new StudentRepositoryImpl();
        SubjectRepositoryImpl subjects = new SubjectRepositoryImpl();
        GradeService gradeService = new GradeServiceImpl(students, subjects);
        GradeManager gradeManager = new GradeManager(gradeService, subjects);
        StudentService studentService = new StudentServiceImpl(students);
        StudentManager studentManager = new StudentManager(studentService, gradeManager);
        BulkImportService bulkImportService = new BulkImportService(subjects, studentManager, gradeManager);
        Student student = students.getAllStudents().get(0);

        otherFormatFilename = "test-import-json-" + System.nanoTime();
        otherFormatExtension = "json";
        new java.io.File("imports").mkdirs();
        gradeDataExporter.exportJson(List.of(
                new GradeRecord("GRD901", student.getStudentId(), "MATH01", 85.0, "01-01-2026"),
                new GradeRecord("GRD902", "NOPE999", "MATH01", 70.0, "01-01-2026"),
                new GradeRecord("GRD903", student.getStudentId(), "MATH01", 150.0, "01-01-2026")
        ), Path.of("imports/" + otherFormatFilename + ".json"));

        BulkImportService.ImportResult result = bulkImportService.importFromFile(otherFormatFilename);
        logFilename = result.getLogFilename();

        assertEquals(1, result.getSuccessCount());
        assertEquals(2, result.getFailedCount());
        assertEquals(1, gradeManager.getGradeCount());
        assertTrue(result.getFailReasons().stream().anyMatch(r -> r.contains("Invalid student ID (NOPE999)")));
        assertTrue(result.getFailReasons().stream().anyMatch(r -> r.contains("Grade must be between 0 and 100")));
    }

    @Test
    @DisplayName("importFromFile() auto-detects a .dat (binary) file and imports its valid records")
    void importFromFileBinaryImportsValidRecordsTest() {
        StudentRepositoryImpl students = new StudentRepositoryImpl();
        SubjectRepositoryImpl subjects = new SubjectRepositoryImpl();
        GradeService gradeService = new GradeServiceImpl(students, subjects);
        GradeManager gradeManager = new GradeManager(gradeService, subjects);
        StudentService studentService = new StudentServiceImpl(students);
        StudentManager studentManager = new StudentManager(studentService, gradeManager);
        BulkImportService bulkImportService = new BulkImportService(subjects, studentManager, gradeManager);
        Student student = students.getAllStudents().get(0);

        otherFormatFilename = "test-import-bin-" + System.nanoTime();
        otherFormatExtension = "dat";
        new java.io.File("imports").mkdirs();
        gradeDataExporter.exportBinary(List.of(
                new GradeRecord("GRD904", student.getStudentId(), "MATH01", 90.0, "01-01-2026")
        ), Path.of("imports/" + otherFormatFilename + ".dat"));

        BulkImportService.ImportResult result = bulkImportService.importFromFile(otherFormatFilename);
        logFilename = result.getLogFilename();

        assertEquals(1, result.getSuccessCount());
        assertEquals(0, result.getFailedCount());
        assertEquals(1, gradeManager.getGradeCount());
    }

    @Test
    @DisplayName("importFromFile() prefers a .csv file over a .json file with the same base name")
    void importFromFilePrefersCsvOverJsonTest() throws IOException {
        StudentRepositoryImpl students = new StudentRepositoryImpl();
        SubjectRepositoryImpl subjects = new SubjectRepositoryImpl();
        GradeService gradeService = new GradeServiceImpl(students, subjects);
        GradeManager gradeManager = new GradeManager(gradeService, subjects);
        StudentService studentService = new StudentServiceImpl(students);
        StudentManager studentManager = new StudentManager(studentService, gradeManager);
        BulkImportService bulkImportService = new BulkImportService(subjects, studentManager, gradeManager);
        Student student = students.getAllStudents().get(0);

        csvFilename = "test-import-priority-" + System.nanoTime();
        otherFormatFilename = csvFilename;
        otherFormatExtension = "json";
        writeCsv(csvFilename, "StudentID,SubjectName,SubjectType,Grade\n"
                + student.getStudentId() + ",Mathematics,Core,85\n");
        gradeDataExporter.exportJson(List.of(
                new GradeRecord("GRD905", student.getStudentId(), "MATH01", 60.0, "01-01-2026"),
                new GradeRecord("GRD906", student.getStudentId(), "MATH01", 61.0, "01-01-2026")
        ), Path.of("imports/" + csvFilename + ".json"));

        BulkImportService.ImportResult result = bulkImportService.importFromFile(csvFilename);
        logFilename = result.getLogFilename();

        // The JSON file has two records; the CSV file has one. Getting exactly
        // one success back confirms the CSV file was the one actually read.
        assertEquals(1, result.getSuccessCount());
        assertEquals(1, gradeManager.getGradeCount());
    }
}
