package main.backup;

import main.dataio.GradeRecord;
import main.dataio.StudentRecord;

import java.util.List;

/** The full contents of one backup file: a manifest header plus every student and grade record. */
public record BackupPayload(BackupManifest manifest, List<StudentRecord> students, List<GradeRecord> grades) {
}
