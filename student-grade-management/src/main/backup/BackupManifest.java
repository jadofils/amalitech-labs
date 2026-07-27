package main.backup;

/** Small header describing a backup file: format version, when it was made, and a quick record count for sanity display. */
public record BackupManifest(int version, String createdAt, int studentCount, int gradeCount) {
}
