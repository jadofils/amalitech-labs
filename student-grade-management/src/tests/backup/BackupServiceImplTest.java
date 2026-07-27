package tests.backup;

import main.backup.BackupPayload;
import main.backup.BackupService;
import main.backup.BackupServiceImpl;
import main.dataio.GradeRecord;
import main.dataio.StudentRecord;
import main.exceptions.BackupErrorCode;
import main.exceptions.BackupException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

// BackupServiceImpl has no injected collaborators (Jackson's ObjectMapper is created internally,
// not passed in) - same reasoning as StudentDataIOTest/GradeDataIOTest (see tests/README.md), so
// there is no Mockito counterpart to this file.
class BackupServiceImplTest {

    private final Path tempDir = Path.of("target/test-backup-" + System.nanoTime());
    private final BackupService backupService = new BackupServiceImpl();

    private final List<StudentRecord> sampleStudents = List.of(
            new StudentRecord("STU001", "Alice Johnson", "REGULAR", 16, "alice@school.edu", "1234567890", "ACTIVE"));
    private final List<GradeRecord> sampleGrades = List.of(
            new GradeRecord("GRD001", "STU001", "MATH01", 85.0, "01-01-2026"));

    @AfterEach
    void cleanUp() throws IOException {
        if (Files.exists(tempDir)) {
            try (var files = Files.walk(tempDir)) {
                files.sorted(java.util.Comparator.reverseOrder()).forEach(p -> {
                    try {
                        Files.deleteIfExists(p);
                    } catch (IOException ignored) {
                        // best-effort cleanup
                    }
                });
            }
        }
    }

    @Test
    @DisplayName("createBackup() then restoreBackup() reproduces the original students and grades exactly")
    void createThenRestoreRoundTripsTest() throws IOException, BackupException {
        Files.createDirectories(tempDir);
        Path path = tempDir.resolve("backup.json");

        backupService.createBackup(path, sampleStudents, sampleGrades);
        BackupPayload restored = backupService.restoreBackup(path);

        assertEquals(sampleStudents, restored.students());
        assertEquals(sampleGrades, restored.grades());
        assertEquals(BackupService.CURRENT_VERSION, restored.manifest().version());
        assertEquals(1, restored.manifest().studentCount());
        assertEquals(1, restored.manifest().gradeCount());
        assertNotNull(restored.manifest().createdAt());
    }

    @Test
    @DisplayName("createBackup() throws a BackupException with IO_FAILURE when the target directory doesn't exist")
    void createBackupIoFailureTest() {
        Path path = tempDir.resolve("does-not-exist").resolve("backup.json");

        BackupException ex = assertThrows(BackupException.class,
                () -> backupService.createBackup(path, sampleStudents, sampleGrades));

        assertEquals(BackupErrorCode.IO_FAILURE, ex.getErrorCode());
        assertEquals(path.toString(), ex.getFilePath());
    }

    @Test
    @DisplayName("restoreBackup() throws a BackupException with IO_FAILURE for a missing file")
    void restoreBackupMissingFileIsIoFailureTest() {
        Path missing = tempDir.resolve("missing.json");

        BackupException ex = assertThrows(BackupException.class, () -> backupService.restoreBackup(missing));

        assertEquals(BackupErrorCode.IO_FAILURE, ex.getErrorCode());
    }

    @Test
    @DisplayName("restoreBackup() throws a BackupException with CORRUPT_FILE for invalid JSON content")
    void restoreBackupCorruptFileTest() throws IOException {
        Files.createDirectories(tempDir);
        Path path = tempDir.resolve("corrupt.json");
        Files.writeString(path, "{ this is not valid JSON", StandardCharsets.UTF_8);

        BackupException ex = assertThrows(BackupException.class, () -> backupService.restoreBackup(path));

        assertEquals(BackupErrorCode.CORRUPT_FILE, ex.getErrorCode());
    }

    @Test
    @DisplayName("restoreBackup() throws a BackupException with VERSION_MISMATCH for an incompatible format version")
    void restoreBackupVersionMismatchTest() throws IOException {
        Files.createDirectories(tempDir);
        Path path = tempDir.resolve("old-version.json");
        Files.writeString(path, """
                {
                  "manifest": {"version": 99, "createdAt": "01-01-2026 00:00:00", "studentCount": 0, "gradeCount": 0},
                  "students": [],
                  "grades": []
                }
                """, StandardCharsets.UTF_8);

        BackupException ex = assertThrows(BackupException.class, () -> backupService.restoreBackup(path));

        assertEquals(BackupErrorCode.VERSION_MISMATCH, ex.getErrorCode());
    }
}
