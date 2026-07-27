package tests.exceptions;

import main.exceptions.BackupErrorCode;
import main.exceptions.BackupException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Directly exercises {@link BackupException}'s constructors and getters - the one custom
 * exception in this application that is deliberately CHECKED, unlike every {@code
 * ApplicationException} subtype covered by {@link ApplicationExceptionHierarchyTest}.
 */
class BackupExceptionTest {

    @Test
    @DisplayName("BackupException is a checked Exception, not an unchecked RuntimeException")
    void isCheckedNotUncheckedTest() {
        // BackupException is statically unrelated to RuntimeException/ApplicationException - unlike
        // every other custom exception in this app, catching it (or declaring `throws`) is
        // compiler-enforced at every call site, which an `instanceof` check against either type
        // wouldn't even compile: it deliberately does NOT extend either class.
        assertEquals(Exception.class, BackupException.class.getSuperclass());
    }

    @Test
    @DisplayName("The message+errorCode+filePath constructor sets every field, with no cause")
    void messageConstructorTest() {
        BackupException ex = new BackupException("corrupt", BackupErrorCode.CORRUPT_FILE, "backups/bad.json");

        assertEquals("corrupt", ex.getMessage());
        assertEquals(BackupErrorCode.CORRUPT_FILE, ex.getErrorCode());
        assertEquals("backups/bad.json", ex.getFilePath());
        assertNull(ex.getCause());
    }

    @Test
    @DisplayName("The message+errorCode+filePath+cause constructor also carries the underlying cause")
    void causeConstructorTest() {
        Throwable cause = new java.io.IOException("disk full");

        BackupException ex = new BackupException("io failure", BackupErrorCode.IO_FAILURE, "backups/x.json", cause);

        assertEquals("io failure", ex.getMessage());
        assertEquals(BackupErrorCode.IO_FAILURE, ex.getErrorCode());
        assertEquals("backups/x.json", ex.getFilePath());
        assertSame(cause, ex.getCause());
    }

    @Test
    @DisplayName("All three BackupErrorCode values are distinct")
    void errorCodeValuesTest() {
        assertEquals(3, BackupErrorCode.values().length);
        assertNotEquals(BackupErrorCode.IO_FAILURE, BackupErrorCode.CORRUPT_FILE);
        assertNotEquals(BackupErrorCode.CORRUPT_FILE, BackupErrorCode.VERSION_MISMATCH);
        assertNotEquals(BackupErrorCode.IO_FAILURE, BackupErrorCode.VERSION_MISMATCH);
    }
}
