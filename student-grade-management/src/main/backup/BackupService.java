package main.backup;

import main.dataio.GradeRecord;
import main.dataio.StudentRecord;
import main.exceptions.BackupException;

import java.nio.file.Path;
import java.util.List;

/**
 * Creates and restores a single-file JSON snapshot of every student and grade record. Both
 * operations are declared {@code throws BackupException} - a genuinely CHECKED exception, unlike
 * the rest of this application's {@link main.exceptions.ApplicationException} hierarchy - so every
 * caller must explicitly decide how to handle a failed backup or restore rather than letting it
 * silently propagate the way an unanticipated runtime failure would.
 */
public interface BackupService {

    /** The backup file format's current version; {@link #restoreBackup} rejects any other value. */
    int CURRENT_VERSION = 1;

    void createBackup(Path path, List<StudentRecord> students, List<GradeRecord> grades) throws BackupException;

    BackupPayload restoreBackup(Path path) throws BackupException;
}
