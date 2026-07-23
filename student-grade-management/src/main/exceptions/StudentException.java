package main.exceptions;

/**
 * General-purpose exception for a Student-related failure that isn't a
 * lookup miss ({@link StudentNotFoundException}) or a field-validation
 * failure ({@link StudentValidationException}) - e.g. the main.repository's
 * fixed-size backing array being full. Mirrors {@link GradeException}'s
 * role as the catch-all for its own entity.
 */
public class StudentException extends ApplicationException {
    private final String studentId;

    public StudentException(String message, String studentId) {
        super(message);
        this.studentId = studentId;
    }

    public StudentException(String message) {
        super(message);
        this.studentId = null;
    }

    public String getStudentId() { return studentId; }
}
