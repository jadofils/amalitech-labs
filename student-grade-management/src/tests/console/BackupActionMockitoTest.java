package tests.console;

import main.backup.BackupManifest;
import main.backup.BackupPayload;
import main.backup.BackupService;
import main.console.BackupAction;
import main.dataio.GradeRecord;
import main.dataio.StudentRecord;
import main.exceptions.BackupErrorCode;
import main.exceptions.BackupException;
import main.manager.GradeManager;
import main.manager.StudentManager;
import main.model.student.RegularStudent;
import main.model.student.Student;
import main.model.subject.CoreSubject;
import main.model.subject.Subject;
import main.repository.subject.SubjectRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.List;
import java.util.Scanner;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Mocks StudentManager/GradeManager/SubjectRepository/BackupService to verify BackupAction's
 * branching in isolation: the empty-filename short-circuits, and every {@link BackupException}
 * error code being caught internally and reported, rather than propagating out of {@code
 * execute()} - none of the real, file-backed {@code BackupServiceImpl}'s scenarios need to be
 * reconstructed on disk just to exercise these branches.
 */
class BackupActionMockitoTest {

    private String runWithInput(StudentManager studentManager, GradeManager gradeManager,
                                 SubjectRepository subjectRepository, BackupService backupService,
                                 String scriptedInput) {
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
    @DisplayName("Empty filename on create prints a message and never calls createBackup()")
    void emptyFilenameOnCreateNeverCallsServiceTest() throws BackupException {
        StudentManager studentManager = mock(StudentManager.class);
        GradeManager gradeManager = mock(GradeManager.class);
        SubjectRepository subjectRepository = mock(SubjectRepository.class);
        BackupService backupService = mock(BackupService.class);

        String output = runWithInput(studentManager, gradeManager, subjectRepository, backupService, "1\n\n\n");

        assertTrue(output.contains("Filename cannot be empty."));
        verify(backupService, never()).createBackup(any(), any(), any());
    }

    @Test
    @DisplayName("Empty filename on restore prints a message and never calls restoreBackup()")
    void emptyFilenameOnRestoreNeverCallsServiceTest() throws BackupException {
        StudentManager studentManager = mock(StudentManager.class);
        GradeManager gradeManager = mock(GradeManager.class);
        SubjectRepository subjectRepository = mock(SubjectRepository.class);
        BackupService backupService = mock(BackupService.class);

        String output = runWithInput(studentManager, gradeManager, subjectRepository, backupService, "2\n\n\n");

        assertTrue(output.contains("Filename cannot be empty."));
        verify(backupService, never()).restoreBackup(any());
    }

    @Test
    @DisplayName("createBackup() gathers every student and grade before delegating to BackupService")
    void createBackupGathersCurrentDataTest() throws BackupException {
        StudentManager studentManager = mock(StudentManager.class);
        GradeManager gradeManager = mock(GradeManager.class);
        SubjectRepository subjectRepository = mock(SubjectRepository.class);
        BackupService backupService = mock(BackupService.class);
        Student student = new RegularStudent("STU001", "Alice Johnson", 16, "alice@school.edu",
                "1234567890", main.model.enums.StudentStatus.ACTIVE);
        when(studentManager.getAllStudents()).thenReturn(List.of(student));
        when(gradeManager.getAllGrades()).thenReturn(List.of());

        String output = runWithInput(studentManager, gradeManager, subjectRepository, backupService,
                "1\nmy-backup\n\n");

        assertTrue(output.contains("Backup created"));
        assertTrue(output.contains("Students: 1, Grades: 0"));
        verify(backupService, times(1)).createBackup(eq(Path.of("backups", "my-backup.json")),
                argThat(students -> students.size() == 1), argThat(List::isEmpty));
    }

    @Test
    @DisplayName("A BackupException from createBackup() is caught internally and its error code is reported")
    void createBackupExceptionIsCaughtAndReportedTest() throws BackupException {
        StudentManager studentManager = mock(StudentManager.class);
        GradeManager gradeManager = mock(GradeManager.class);
        SubjectRepository subjectRepository = mock(SubjectRepository.class);
        BackupService backupService = mock(BackupService.class);
        when(studentManager.getAllStudents()).thenReturn(List.of());
        when(gradeManager.getAllGrades()).thenReturn(List.of());
        doThrow(new BackupException("disk full", BackupErrorCode.IO_FAILURE, "backups/my-backup.json"))
                .when(backupService).createBackup(any(), any(), any());

        String output = runWithInput(studentManager, gradeManager, subjectRepository, backupService,
                "1\nmy-backup\n\n");

        assertTrue(output.contains("ERROR: BackupException"));
        assertTrue(output.contains("IO_FAILURE"));
        assertTrue(output.contains("disk full"));
    }

    @Test
    @DisplayName("A CORRUPT_FILE BackupException from restoreBackup() is caught internally and reported")
    void restoreBackupCorruptFileIsCaughtAndReportedTest() throws BackupException {
        StudentManager studentManager = mock(StudentManager.class);
        GradeManager gradeManager = mock(GradeManager.class);
        SubjectRepository subjectRepository = mock(SubjectRepository.class);
        BackupService backupService = mock(BackupService.class);
        when(backupService.restoreBackup(any()))
                .thenThrow(new BackupException("malformed JSON", BackupErrorCode.CORRUPT_FILE, "backups/bad.json"));

        String output = runWithInput(studentManager, gradeManager, subjectRepository, backupService,
                "2\nbad\n\n");

        assertTrue(output.contains("ERROR: BackupException"));
        assertTrue(output.contains("CORRUPT_FILE"));
        verify(studentManager, never()).addStudent(any());
        verify(gradeManager, never()).addGrade(any());
    }

    @Test
    @DisplayName("A VERSION_MISMATCH BackupException from restoreBackup() is caught internally and reported")
    void restoreBackupVersionMismatchIsCaughtAndReportedTest() throws BackupException {
        StudentManager studentManager = mock(StudentManager.class);
        GradeManager gradeManager = mock(GradeManager.class);
        SubjectRepository subjectRepository = mock(SubjectRepository.class);
        BackupService backupService = mock(BackupService.class);
        when(backupService.restoreBackup(any()))
                .thenThrow(new BackupException("unsupported version", BackupErrorCode.VERSION_MISMATCH, "backups/old.json"));

        String output = runWithInput(studentManager, gradeManager, subjectRepository, backupService,
                "2\nold\n\n");

        assertTrue(output.contains("ERROR: BackupException"));
        assertTrue(output.contains("VERSION_MISMATCH"));
    }

    @Test
    @DisplayName("restoreBackup() re-adds every restored student and grade through the real mappers")
    void restoreBackupReAddsEveryRecordTest() throws BackupException {
        StudentManager studentManager = mock(StudentManager.class);
        GradeManager gradeManager = mock(GradeManager.class);
        SubjectRepository subjectRepository = mock(SubjectRepository.class);
        BackupService backupService = mock(BackupService.class);
        Subject math = new CoreSubject("Mathematics", "MATH01");
        when(subjectRepository.findSubjectByCode("MATH01")).thenReturn(math);
        StudentRecord studentRecord = new StudentRecord("STU001", "Alice Johnson", "REGULAR", 16,
                "alice@school.edu", "1234567890", "ACTIVE");
        GradeRecord gradeRecord = new GradeRecord("GRD001", "STU001", "MATH01", 85.0, "01-01-2026");
        BackupPayload payload = new BackupPayload(
                new BackupManifest(BackupService.CURRENT_VERSION, "01-01-2026 00:00:00", 1, 1),
                List.of(studentRecord), List.of(gradeRecord));
        when(backupService.restoreBackup(Path.of("backups", "good.json"))).thenReturn(payload);

        String output = runWithInput(studentManager, gradeManager, subjectRepository, backupService,
                "2\ngood\n\n");

        assertTrue(output.contains("Restored from backup"));
        assertTrue(output.contains("Students: 1, Grades: 1"));
        verify(studentManager, times(1)).addStudent(argThat(s -> s.getStudentId().equals("STU001")));
        verify(gradeManager, times(1)).addGrade(argThat(g -> g.getGradeId().equals("GRD001")));
    }

    @Test
    @DisplayName("getOptionNumber(), getLabel() and isAuthorizedFor() report the expected menu metadata")
    void menuMetadataTest() {
        BackupAction action = new BackupAction(new Scanner(new ByteArrayInputStream(new byte[0])),
                mock(StudentManager.class), mock(GradeManager.class), mock(SubjectRepository.class),
                mock(BackupService.class));

        assertEquals(11, action.getOptionNumber());
        assertEquals("Backup & Restore", action.getLabel());
        assertTrue(action.isAuthorizedFor(main.model.enums.Role.TEACHER));
        assertFalse(action.isAuthorizedFor(main.model.enums.Role.STUDENT));
    }
}
