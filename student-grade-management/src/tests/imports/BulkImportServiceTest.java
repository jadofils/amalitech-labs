package tests.imports;

import exceptions.ImportException;
import imports.BulkImportService;
import manager.GradeManager;
import manager.StudentManager;
import model.student.Student;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import repository.student.StudentRepositoryImpl;
import repository.subject.SubjectRepositoryImpl;
import service.GradeService;
import service.StudentService;
import service.GradeServiceImpl;
import service.StudentServiceImpl;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class BulkImportServiceTest {

    private String csvFilename;
    private String logFilename;

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

        assertThrows(ImportException.class,
                () -> bulkImportService.importFromFile("does-not-exist-" + System.nanoTime()));
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
}
