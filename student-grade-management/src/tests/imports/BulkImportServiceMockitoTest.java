package tests.imports;

import imports.BulkImportService;
import manager.GradeManager;
import manager.StudentManager;
import model.grade.Grade;
import model.student.RegularStudent;
import model.student.Student;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import repository.subject.impl.SubjectRepositoryImpl;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Mocks StudentManager and GradeManager to verify BulkImportService's own
 * row-by-row orchestration (look up the student, then record the grade)
 * independent of their real implementations. CSVParser itself stays real,
 * since BulkImportService constructs it internally rather than accepting
 * one via injection - so these tests still touch a real (temporary) CSV file.
 */
class BulkImportServiceMockitoTest {

    private final SubjectRepositoryImpl subjects = new SubjectRepositoryImpl();
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
    @DisplayName("A valid row looks up the student and records exactly one grade")
    void validRowLooksUpStudentAndRecordsGradeTest() throws IOException {
        StudentManager studentManager = mock(StudentManager.class);
        GradeManager gradeManager = mock(GradeManager.class);
        Student student = new RegularStudent("STU001", "Musa Nkusi", 17, "musa@school.edu",
                "1234567890", model.enums.StudentStatus.ACTIVE);
        when(studentManager.findStudent("STU001")).thenReturn(student);
        BulkImportService service = new BulkImportService(subjects, studentManager, gradeManager);

        csvFilename = "mockito-import-" + System.nanoTime();
        writeCsv(csvFilename, "StudentID,SubjectName,SubjectType,Grade\nSTU001,Mathematics,Core,85\n");

        BulkImportService.ImportResult result = service.importFromFile(csvFilename);
        logFilename = result.getLogFilename();

        assertEquals(1, result.getSuccessCount());
        verify(studentManager, times(1)).findStudent("STU001");
        verify(gradeManager, times(1)).addGrade(any(Grade.class));
    }

    @Test
    @DisplayName("A row for an unknown student never reaches GradeManager.addGrade()")
    void unknownStudentNeverRecordsGradeTest() throws IOException {
        StudentManager studentManager = mock(StudentManager.class);
        GradeManager gradeManager = mock(GradeManager.class);
        when(studentManager.findStudent("NOPE")).thenReturn(null);
        BulkImportService service = new BulkImportService(subjects, studentManager, gradeManager);

        csvFilename = "mockito-import-unknown-" + System.nanoTime();
        writeCsv(csvFilename, "StudentID,SubjectName,SubjectType,Grade\nNOPE,Mathematics,Core,85\n");

        BulkImportService.ImportResult result = service.importFromFile(csvFilename);
        logFilename = result.getLogFilename();

        assertEquals(0, result.getSuccessCount());
        assertEquals(1, result.getFailedCount());
        verify(gradeManager, never()).addGrade(any());
    }
}
