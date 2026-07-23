package dataio;

import java.io.Serializable;

/**
 * A full-fidelity, format-agnostic snapshot of a {@link model.grade.Grade} - mirrors
 * {@link StudentRecord}'s role for the grade side of US-2. Stores {@code subjectCode} rather than
 * the full {@link model.subject.Subject} object, the same way the existing CSV bulk-import path
 * already does (see {@code imports.CSVParser}) - reconstructing a {@link model.grade.Grade} needs
 * a subject repository lookup, which is exactly why that step lives in
 * {@link GradeRecordMapper#toGrade}, not here.
 */
public record GradeRecord(
        String gradeId,
        String studentId,
        String subjectCode,
        double grade,
        String date
) implements Serializable {
}
