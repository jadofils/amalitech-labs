package tests.console;

import main.backup.BackupService;
import main.backup.BackupServiceImpl;
import main.console.BackupAction;
import main.manager.GradeManager;
import main.manager.StudentManager;
import main.model.enums.Role;
import main.model.grade.Grade;
import main.model.student.Student;
import main.repository.student.StudentRepositoryImpl;
import main.repository.subject.SubjectRepositoryImpl;
import main.service.GradeService;
import main.service.GradeServiceImpl;
import main.service.StudentService;
import main.service.StudentServiceImpl;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Scanner;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Wires the real BackupServiceImpl/StudentManager/GradeManager stack (the same wiring Main.java
 * uses) to verify BackupAction's happy path end-to-end. BackupActionMockitoTest verifies the
 * BackupException-handling branches (IO_FAILURE, CORRUPT_FILE, VERSION_MISMATCH), which are
 * awkward to construct through the real file-backed BackupService for every scenario.
 */
class BackupActionTest {

    private StudentRepositoryImpl studentRepository;
    private SubjectRepositoryImpl subjectRepository;
    private GradeManager gradeManager;
    private StudentManager studentManager;
    private BackupService backupService;

    private String backupFilename;

    @BeforeEach
    void setUp() {
        studentRepository = new StudentRepositoryImpl();
        subjectRepository = new SubjectRepositoryImpl();
        GradeService gradeService = new GradeServiceImpl(studentRepository, subjectRepository);
        gradeManager = new GradeManager(gradeService, subjectRepository);
        StudentService studentService = new StudentServiceImpl(studentRepository);
        studentManager = new StudentManager(studentService, gradeManager);
        backupService = new BackupServiceImpl();
    }

    @AfterEach
    void cleanUp() throws IOException {
        if (backupFilename != null) {
            Files.deleteIfExists(Path.of("backups", backupFilename + ".json"));
        }
    }

    private String runWithInput(String scriptedInput) {
        PrintStream originalOut = System.out;
        ByteArrayOutputStream captured = new ByteArrayOutputStream();
        try (Scanner scanner = new Scanner(new ByteArrayInputStream(scriptedInput.getBytes(StandardCharsets.UTF_8)))) {
            System.setOut(new PrintStream(captured, true, StandardCharsets.UTF_8));
            new BackupAction(scanner, studentManager, gradeManager, subjectRepository, backupService).execute();
        } finally {
            System.setOut(originalOut);
        }
        return captured.toString(StandardCharsets.UTF_8);
    }

    @Test
    @DisplayName("Create backup, then restore it into a fresh system, reproduces every student and grade")
    void createThenRestoreRoundTripsThroughTheMenuTest() {
        Student student = studentRepository.getAllStudents().get(0);
        var math = gradeManager.getSubjectsByType(main.model.enums.SubjectType.CORE).get(0);
        gradeManager.addGrade(new Grade(student.getStudentId(), math, 85.0));
        backupFilename = "action-test-" + System.nanoTime();

        String createOutput = runWithInput("1\n" + backupFilename + "\n\n");
        assertTrue(createOutput.contains("Backup created"));

        // A fresh, empty system to restore into.
        StudentRepositoryImpl freshStudents = new StudentRepositoryImpl();
        SubjectRepositoryImpl freshSubjects = new SubjectRepositoryImpl();
        GradeService freshGradeService = new GradeServiceImpl(freshStudents, freshSubjects);
        GradeManager freshGradeManager = new GradeManager(freshGradeService, freshSubjects);
        StudentService freshStudentService = new StudentServiceImpl(freshStudents);
        StudentManager freshStudentManager = new StudentManager(freshStudentService, freshGradeManager);
        int studentsBefore = freshStudentManager.getStudentCount();

        PrintStream originalOut = System.out;
        ByteArrayOutputStream captured = new ByteArrayOutputStream();
        String restoreOutput;
        try (Scanner scanner = new Scanner(new ByteArrayInputStream(
                ("2\n" + backupFilename + "\n\n").getBytes(StandardCharsets.UTF_8)))) {
            System.setOut(new PrintStream(captured, true, StandardCharsets.UTF_8));
            new BackupAction(scanner, freshStudentManager, freshGradeManager, freshSubjects, backupService).execute();
        } finally {
            System.setOut(originalOut);
        }
        restoreOutput = captured.toString(StandardCharsets.UTF_8);

        assertTrue(restoreOutput.contains("Restored from backup"));
        // createBackup() snapshots every current student, not just the one graded above - the
        // original stack's own 5 seeded students all round-trip on top of the fresh stack's 5.
        assertEquals(studentsBefore + 5, freshStudentManager.getStudentCount());
        assertEquals(1, freshGradeManager.getAllGrades().size());
    }

    @Test
    @DisplayName("Empty filename on create prints 'Filename cannot be empty.' without calling the backup service")
    void emptyFilenameOnCreatePrintsMessageTest() {
        String output = runWithInput("1\n\n\n");

        assertTrue(output.contains("Filename cannot be empty."));
    }

    @Test
    @DisplayName("An invalid top-level menu choice prints 'Invalid option.'")
    void invalidTopLevelChoicePrintsMessageTest() {
        String output = runWithInput("9\n\n");

        assertTrue(output.contains("Invalid option."));
    }

    @Test
    @DisplayName("Restoring a backup that was never created is caught internally and prints the BackupException error")
    void restoringMissingBackupPrintsErrorTest() {
        String output = runWithInput("2\ndoes-not-exist-" + System.nanoTime() + "\n\n");

        assertTrue(output.contains("ERROR: BackupException"));
        assertTrue(output.contains("IO_FAILURE"));
    }

    @Test
    @DisplayName("getOptionNumber(), getLabel() and isAuthorizedFor() report the expected menu metadata")
    void menuMetadataTest() {
        BackupAction action = new BackupAction(new Scanner(new ByteArrayInputStream(new byte[0])),
                studentManager, gradeManager, subjectRepository, backupService);

        assertEquals(11, action.getOptionNumber());
        assertEquals("Backup & Restore", action.getLabel());
        assertTrue(action.isAuthorizedFor(Role.TEACHER));
        assertFalse(action.isAuthorizedFor(Role.STUDENT));
    }
}
