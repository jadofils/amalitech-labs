package main.exceptions;

/**
 * Thrown by {@code main.backup.BackupService} when a backup can't be created or restored.
 * Deliberately a CHECKED exception (extends {@link Exception}, not {@link ApplicationException},
 * which extends {@link RuntimeException}) - unlike every other exception in this application, a
 * caller must explicitly catch or declare this one; the compiler enforces it. Backup/restore
 * failures are exactly the kind of "recoverable, caller must decide how" condition checked
 * exceptions exist for: a version mismatch might mean "use an older client", a corrupt file might
 * mean "try an earlier backup", and an I/O failure might mean "check disk space and retry" - three
 * different recovery paths a single unchecked catch-all would blur together.
 */
public class BackupException extends Exception {

    private final BackupErrorCode errorCode;
    private final String filePath;

    public BackupException(String message, BackupErrorCode errorCode, String filePath) {
        super(message);
        this.errorCode = errorCode;
        this.filePath = filePath;
    }

    public BackupException(String message, BackupErrorCode errorCode, String filePath, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
        this.filePath = filePath;
    }

    public BackupErrorCode getErrorCode() {
        return errorCode;
    }

    public String getFilePath() {
        return filePath;
    }
}
