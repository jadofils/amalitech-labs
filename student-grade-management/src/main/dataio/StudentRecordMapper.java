package main.dataio;

import main.model.enums.StudentStatus;
import main.model.enums.StudentType;
import main.model.student.HonorsStudent;
import main.model.student.RegularStudent;
import main.model.student.Student;

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
    public static Student toStudent(StudentRecord studentRecord) {
        StudentStatus status = StudentStatus.valueOf(studentRecord.status());
        StudentType type = StudentType.valueOf(studentRecord.studentType());
        if (type == StudentType.HONORS) {
            return new HonorsStudent(studentRecord.studentId(), studentRecord.name(), studentRecord.age(),
                    studentRecord.email(), studentRecord.phone(), status);
        }
        return new RegularStudent(studentRecord.studentId(), studentRecord.name(), studentRecord.age(),
                studentRecord.email(), studentRecord.phone(), status);
    }
}
