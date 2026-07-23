package dataio;

import model.enums.StudentStatus;
import model.enums.StudentType;
import model.student.HonorsStudent;
import model.student.RegularStudent;
import model.student.Student;

/**
 * Converts between the domain {@link Student} hierarchy and the flat, format-agnostic
 * {@link StudentRecord} used for CSV/JSON/binary export-import. Kept separate from
 * {@link mapper.StudentMapper} (which maps to the display-only {@link dto.StudentDTO}) since this
 * mapper has a different job - round-tripping every field, both directions - and a different
 * reason to change.
 */
public final class StudentRecordMapper {

    private StudentRecordMapper() {
    }

    public static StudentRecord toRecord(Student student) {
        return new StudentRecord(
                student.getStudentId(),
                student.getName(),
                student.getType().name(),
                student.getAge(),
                student.getEmail(),
                student.getPhone(),
                student.getStatus().name()
        );
    }

    /** Reconstructs the concrete {@link Student} subclass indicated by {@link StudentRecord#studentType()}. */
    public static Student toStudent(StudentRecord record) {
        StudentStatus status = StudentStatus.valueOf(record.status());
        StudentType type = StudentType.valueOf(record.studentType());
        if (type == StudentType.HONORS) {
            return new HonorsStudent(record.studentId(), record.name(), record.age(),
                    record.email(), record.phone(), status);
        }
        return new RegularStudent(record.studentId(), record.name(), record.age(),
                record.email(), record.phone(), status);
    }
}
