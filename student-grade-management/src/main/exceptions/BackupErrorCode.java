package main.exceptions;

/** Distinguishes why a backup operation failed, so a caller can choose a different recovery path for each. */
public enum BackupErrorCode {
    /** The backup file couldn't be read or written (disk full, permissions, missing path, ...). */
    IO_FAILURE,
    /** The file exists and was readable, but its content isn't a valid backup payload. */
    CORRUPT_FILE,
    /** The file is a valid backup payload, but from an incompatible format version. */
    VERSION_MISMATCH
}
